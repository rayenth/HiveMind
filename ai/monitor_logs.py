"""
Microservice de surveillance et traitement automatique de logs
Version Pro - Avec IA, API, gestion d'erreurs et monitoring
"""

import os
import time
import json
import logging
import argparse
import hashlib
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional, Tuple
import sys

# Import local
try:
    from detect_anomaly import detect_anomaly
    AI_AVAILABLE = True
except ImportError:
    AI_AVAILABLE = False
    print("⚠️  Module detect_anomaly non disponible - mode simulation activé")

# ============================================================================
# CONFIGURATION
# ============================================================================

# Configuration par défaut
DEFAULT_CONFIG = {
    "log_dir": "logs",
    "archive_dir": "archive",
    "results_dir": "results",
    "check_interval": 2,  # secondes
    "max_file_size_mb": 10,
    "supported_extensions": [".log", ".txt", ".json", ".csv"],
    "enable_ai": True,
    "ai_model": "llama3:latest",
    "use_api": False,
    "api_url": "http://localhost:5001/api/v1/analyze",  # Corrigé le endpoint
    "delete_after_processing": False,  # Sinon archive
    "retry_on_error": True,
    "max_retries": 3
}

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('monitor.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# ============================================================================
# CLASSES ET FONCTIONS UTILITAIRES
# ============================================================================

class LogMonitor:
    """Classe principale de surveillance de logs"""
    
    def __init__(self, config: Dict = None):
        self.config = DEFAULT_CONFIG.copy()
        if config:
            self.config.update(config)
        
        # Initialisation des dossiers
        self.ensure_directories()
        
        # Suivi des fichiers traités (par empreinte MD5)
        self.processed_files = set()
        self.load_processed_files()
        
        # Suivi des tentatives de traitement
        self.processing_attempts = {}
        
        # Statistiques
        self.stats = {
            "files_processed": 0,
            "lines_processed": 0,
            "anomalies_detected": 0,
            "errors": 0,
            "warnings": 0,
            "start_time": datetime.now().isoformat()
        }
        
        logger.info(f"LogMonitor initialisé - Dossier: {self.config['log_dir']}")
    
    def ensure_directories(self):
        """Crée les dossiers nécessaires s'ils n'existent pas"""
        for dir_key in ['log_dir', 'archive_dir', 'results_dir']:
            dir_path = self.config[dir_key]
            Path(dir_path).mkdir(exist_ok=True, parents=True)
            logger.debug(f"Dossier vérifié/créé: {dir_path}")
    
    def load_processed_files(self):
        """Charge l'historique des fichiers traités"""
        history_file = Path(self.config['results_dir']) / "processing_history.json"
        if history_file.exists():
            try:
                with open(history_file, 'r', encoding='utf-8') as f:
                    history = json.load(f)
                    self.processed_files = set(history.get("processed_files", []))
                logger.info(f"Historique chargé: {len(self.processed_files)} fichiers traités")
            except Exception as e:
                logger.warning(f"Impossible de charger l'historique: {e}")
    
    def save_processed_files(self):
        """Sauvegarde l'historique des fichiers traités"""
        history_file = Path(self.config['results_dir']) / "processing_history.json"
        try:
            history = {
                "processed_files": list(self.processed_files),
                "last_update": datetime.now().isoformat(),
                "total_files": len(self.processed_files),
                "statistics": self.stats
            }
            with open(history_file, 'w', encoding='utf-8') as f:
                json.dump(history, f, indent=2, ensure_ascii=False)
        except Exception as e:
            logger.error(f"Erreur lors de la sauvegarde de l'historique: {e}")
    
    def get_file_hash(self, filepath: str) -> str:
        """Calcule l'empreinte MD5 d'un fichier"""
        try:
            with open(filepath, 'rb') as f:
                return hashlib.md5(f.read()).hexdigest()
        except Exception as e:
            logger.error(f"Erreur lors du calcul du hash: {e}")
            return ""
    
    def is_valid_file(self, filepath: str) -> Tuple[bool, str]:
        """Vérifie si un fichier est valide pour le traitement"""
        path = Path(filepath)
        
        # Vérification que c'est un fichier
        if not path.is_file():
            return False, "N'est pas un fichier"
        
        # Vérification de l'extension
        if path.suffix.lower() not in self.config['supported_extensions']:
            return False, f"Extension non supportée: {path.suffix}"
        
        # Vérification de la taille
        max_size = self.config['max_file_size_mb'] * 1024 * 1024
        file_size = path.stat().st_size
        if file_size > max_size:
            return False, f"Fichier trop volumineux ({file_size/1024/1024:.2f}MB > {self.config['max_file_size_mb']}MB)"
        
        # Vérification si fichier vide
        if file_size == 0:
            return False, "Fichier vide"
        
        return True, "OK"
    
    def read_log_file(self, filepath: str) -> List[str]:
        """
        Lit un fichier log avec gestion d'encodage
        Supporte UTF-8, UTF-16, Windows-1252, etc.
        """
        encodings_to_try = ['utf-8-sig', 'utf-16', 'cp1252', 'latin-1', 'iso-8859-1', 'utf-8']
        
        for encoding in encodings_to_try:
            try:
                with open(filepath, 'r', encoding=encoding) as f:
                    lines = [line.strip() for line in f if line.strip()]
                logger.debug(f"Fichier lu avec encodage: {encoding}")
                return lines
            except UnicodeDecodeError:
                continue
            except Exception as e:
                logger.debug(f"Erreur avec encodage {encoding}: {e}")
                continue
        
        # Fallback avec ignore errors
        try:
            with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                lines = [line.strip() for line in f if line.strip()]
            logger.warning(f"Fichier lu avec ignore errors: {filepath}")
            return lines
        except Exception as e:
            logger.error(f"Impossible de lire le fichier {filepath}: {e}")
            return []
    
    def analyze_log_line(self, line: str) -> Dict:
        """Analyse une ligne de log avec IA ou simulation"""
        
        # Mode API
        if self.config['use_api']:
            try:
                import requests
                response = requests.post(
                    self.config['api_url'],
                    json={"log": line, "model": self.config['ai_model']},
                    timeout=30
                )
                if response.status_code == 200:
                    return response.json().get('data', {}).get('analysis', {})
                else:
                    logger.error(f"Erreur API HTTP {response.status_code}: {response.text}")
                    return {"error": f"API HTTP {response.status_code}", "anomaly": None}
            except Exception as e:
                logger.error(f"Erreur API: {e}")
                return {"error": str(e), "anomaly": None}
        
        # Mode IA direct
        elif self.config['enable_ai'] and AI_AVAILABLE:
            try:
                return detect_anomaly(line, self.config['ai_model'])
            except Exception as e:
                logger.error(f"Erreur IA: {e}")
                return {"error": str(e), "anomaly": None}
        
        # Mode simulation (fallback)
        else:
            # Simulation intelligente basée sur des motifs
            suspicious_keywords = ['fail', 'error', 'denied', 'blocked', 'scan', 'attack', 'malicious', 'intrusion', 'breach']
            warning_keywords = ['warning', 'alert', 'critical', 'exception', 'timeout']
            
            line_lower = line.lower()
            
            if any(keyword in line_lower for keyword in suspicious_keywords):
                return {
                    "anomaly": True,
                    "confidence": 0.8,
                    "reason": "Motif suspect détecté",
                    "category": "suspicious_pattern"
                }
            elif any(keyword in line_lower for keyword in warning_keywords):
                return {
                    "anomaly": False,
                    "confidence": 0.6,
                    "reason": "Avertissement système",
                    "category": "warning"
                }
            else:
                return {
                    "anomaly": False,
                    "confidence": 0.9,
                    "reason": "Log normal",
                    "category": "normal"
                }
    
    def process_file(self, filepath: str, filename: str):
        """Traite un fichier log complet"""
        logger.info(f"Début du traitement: {filename}")
        
        # Suivi des tentatives
        if filename not in self.processing_attempts:
            self.processing_attempts[filename] = 0
        self.processing_attempts[filename] += 1
        
        # Vérifier les tentatives excessives
        if self.processing_attempts[filename] > self.config['max_retries']:
            logger.error(f"Trop de tentatives pour {filename}, fichier ignoré")
            return
        
        # Calculer l'empreinte pour éviter les doublons
        file_hash = self.get_file_hash(filepath)
        if file_hash in self.processed_files:
            logger.info(f"Fichier déjà traité: {filename}")
            # Supprimer le fichier s'il est déjà traité
            try:
                os.remove(filepath)
                logger.info(f"Fichier supprimé (déjà traité): {filename}")
            except:
                pass
            return
        
        # Lire le fichier
        lines = self.read_log_file(filepath)
        if not lines:
            logger.warning(f"Fichier vide ou illisible: {filename}")
            self.stats["warnings"] += 1
            # Supprimer le fichier vide
            try:
                os.remove(filepath)
                logger.info(f"Fichier vide supprimé: {filename}")
            except:
                pass
            return
        
        # Analyser chaque ligne
        results = []
        anomalies = []
        warnings = []
        
        for i, line in enumerate(lines, 1):
            try:
                analysis = self.analyze_log_line(line)
                
                result_entry = {
                    "line_number": i,
                    "original_log": line,
                    "analysis": analysis,
                    "timestamp": datetime.now().isoformat()
                }
                results.append(result_entry)
                
                if analysis.get("anomaly", False):
                    anomalies.append(result_entry)
                    logger.warning(f"⚠️  Anomalie détectée (ligne {i}): {line[:100]}...")
                elif analysis.get("category") == "warning":
                    warnings.append(result_entry)
                    logger.info(f"⚠️  Avertissement (ligne {i}): {line[:80]}...")
                
                # Mise à jour des statistiques
                self.stats["lines_processed"] += 1
                if analysis.get("anomaly", False):
                    self.stats["anomalies_detected"] += 1
                
                # Petit délai pour éviter la surcharge
                time.sleep(0.05)
                
            except Exception as e:
                logger.error(f"Erreur ligne {i}: {e}")
                self.stats["errors"] += 1
        
        # Générer le rapport
        report = self.generate_report(filename, lines, results, anomalies, warnings)
        
        # Sauvegarder les résultats
        self.save_results(filename, report)
        
        # Archiver ou supprimer le fichier
        success = self.archive_file(filepath, filename)
        
        if success:
            # Mettre à jour l'historique seulement si succès
            self.processed_files.add(file_hash)
            self.stats["files_processed"] += 1
        
        logger.info(f"Traitement terminé: {filename} - {len(anomalies)} anomalies, {len(warnings)} avertissements détectés")
    
    def generate_report(self, filename: str, lines: List[str], 
                       results: List[Dict], anomalies: List[Dict], 
                       warnings: List[Dict] = None) -> Dict:
        """Génère un rapport complet d'analyse"""
        if warnings is None:
            warnings = []
        
        total_issues = len(anomalies) + len(warnings)
        
        return {
            "metadata": {
                "filename": filename,
                "processing_date": datetime.now().isoformat(),
                "total_lines": len(lines),
                "processed_lines": len(results),
                "model_used": self.config['ai_model'] if self.config['enable_ai'] else "simulation",
                "mode": "api" if self.config['use_api'] else "direct"
            },
            "statistics": {
                "anomalies_detected": len(anomalies),
                "warnings_detected": len(warnings),
                "total_issues": total_issues,
                "anomaly_percentage": (len(anomalies) / len(results) * 100) if results else 0,
                "issue_percentage": (total_issues / len(results) * 100) if results else 0,
                "most_common_category": max(
                    [r["analysis"].get("category", "unknown") for r in anomalies + warnings],
                    key=[r["analysis"].get("category", "unknown") for r in anomalies + warnings].count,
                    default="none"
                ) if (anomalies or warnings) else "none",
                "average_confidence": sum(
                    r["analysis"].get("confidence", 0) for r in results
                ) / len(results) if results else 0
            },
            "summary": {
                "status": "ANOMALIES_DETECTED" if anomalies else ("WARNINGS" if warnings else "CLEAN"),
                "risk_level": "CRITICAL" if len(anomalies) > 5 else 
                             "HIGH" if len(anomalies) > 2 else 
                             "MEDIUM" if len(anomalies) > 0 or len(warnings) > 3 else 
                             "LOW" if len(warnings) > 0 else 
                             "NONE",
                "recommendation": "Investigation immédiate recommandée" if anomalies else 
                                "Surveillance renforcée recommandée" if warnings else 
                                "Aucune action requise"
            },
            "top_anomalies": anomalies[:10],
            "top_warnings": warnings[:10],
            "sample_results": results[:3] if results else []
        }
    
    def save_results(self, filename: str, report: Dict):
        """Sauvegarde les résultats d'analyse"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        # Nettoyer le nom de fichier pour éviter les caractères problématiques
        safe_filename = "".join(c for c in filename if c.isalnum() or c in (' ', '-', '_', '.')).rstrip()
        result_file = Path(self.config['results_dir']) / f"analysis_{safe_filename}_{timestamp}.json"
        
        try:
            with open(result_file, 'w', encoding='utf-8') as f:
                json.dump(report, f, indent=2, ensure_ascii=False)
            logger.info(f"Résultats sauvegardés: {result_file}")
            return True
        except Exception as e:
            logger.error(f"Erreur lors de la sauvegarde: {e}")
            self.stats["errors"] += 1
            return False
    
    def archive_file(self, filepath: str, filename: str) -> bool:
        """Archive ou supprime le fichier traité - Retourne True si succès"""
        try:
            if self.config['delete_after_processing']:
                os.remove(filepath)
                logger.info(f"Fichier supprimé: {filename}")
                return True
            else:
                # Ajouter un timestamp UNIQUE avec microsecondes
                timestamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
                
                # Nettoyer le nom de fichier
                safe_filename = "".join(c for c in filename if c.isalnum() or c in (' ', '-', '_', '.')).rstrip()
                base_name = safe_filename.rsplit('.', 1)[0] if '.' in safe_filename else safe_filename
                
                archive_name = f"{base_name}_{timestamp}"
                archive_path = Path(self.config['archive_dir']) / archive_name
                
                # Vérifier si existe déjà et ajouter un compteur
                counter = 1
                while archive_path.exists():
                    archive_name = f"{base_name}_{timestamp}_{counter}"
                    archive_path = Path(self.config['archive_dir']) / archive_name
                    counter += 1
                    if counter > 100:  # Limite de sécurité
                        raise Exception("Trop de fichiers avec le même nom dans l'archive")
                
                os.rename(filepath, archive_path)
                logger.info(f"Fichier archivé: {archive_path}")
                return True
                
        except Exception as e:
            logger.error(f"Erreur lors de l'archivage/suppression: {e}")
            self.stats["errors"] += 1
            
            # Option: supprimer si échec d'archivage
            if self.config.get('delete_on_archive_failure', True):
                try:
                    os.remove(filepath)
                    logger.info(f"Fichier supprimé après échec d'archivage: {filename}")
                    return True
                except Exception as delete_error:
                    logger.error(f"Impossible de supprimer le fichier après échec: {delete_error}")
                    return False
            return False
    
    def monitor(self):
        """Boucle principale de surveillance"""
        logger.info("🚀 Démarrage du monitoring...")
        print("\n" + "="*60)
        print("📡 MONITORING DE LOGS ACTIVÉ")
        print("="*60)
        print(f"Dossier surveillé: {self.config['log_dir']}")
        print(f"Modèle IA: {self.config['ai_model']}")
        print(f"Mode: {'API' if self.config['use_api'] else 'Direct'}")
        print(f"Intervalle de vérification: {self.config['check_interval']}s")
        print(f"Archivage: {'SUPPRESSION' if self.config['delete_after_processing'] else 'ARCHIVAGE'}")
        print("="*60)
        print("Appuyez sur Ctrl+C pour arrêter\n")
        
        try:
            while True:
                # Lister les fichiers dans le dossier logs
                log_dir = Path(self.config['log_dir'])
                files_processed_this_cycle = 0
                
                for filepath in log_dir.iterdir():
                    if filepath.is_file():
                        # Vérifier si le fichier est valide
                        is_valid, reason = self.is_valid_file(str(filepath))
                        if not is_valid:
                            logger.debug(f"Fichier ignoré ({reason}): {filepath.name}")
                            continue
                        
                        # Traiter le fichier
                        self.process_file(str(filepath), filepath.name)
                        files_processed_this_cycle += 1
                
                # Sauvegarder périodiquement l'historique
                if self.stats["files_processed"] % 5 == 0:
                    self.save_processed_files()
                
                # Afficher les statistiques périodiquement
                if self.stats["files_processed"] % 10 == 0:
                    self.display_stats()
                
                # Si aucun fichier traité, afficher un heartbeat
                if files_processed_this_cycle == 0:
                    logger.debug("Aucun nouveau fichier détecté...")
                
                # Pause
                time.sleep(self.config['check_interval'])
                
        except KeyboardInterrupt:
            print("\n\n🛑 Arrêt du monitoring...")
            logger.info("Monitoring arrêté par l'utilisateur")
        except Exception as e:
            logger.critical(f"Erreur critique dans la boucle de monitoring: {e}", exc_info=True)
        finally:
            # Nettoyage final
            self.save_processed_files()
            self.display_stats(full=True)
            print("\n✨ Monitoring terminé. Bonne journée !")
    
    def display_stats(self, full: bool = False):
        """Affiche les statistiques"""
        if full:
            print("\n" + "="*60)
            print("📊 STATISTIQUES FINALES")
            print("="*60)
        else:
            print("\n" + "-"*40)
            print("📈 Statistiques en cours...")
        
        runtime = datetime.now() - datetime.fromisoformat(self.stats["start_time"])
        hours, remainder = divmod(runtime.total_seconds(), 3600)
        minutes, seconds = divmod(remainder, 60)
        
        print(f"⏱️  Temps d'exécution: {int(hours)}h {int(minutes)}m {int(seconds)}s")
        print(f"📁 Fichiers traités: {self.stats['files_processed']}")
        print(f"📝 Lignes analysées: {self.stats['lines_processed']}")
        print(f"🚨 Anomalies détectées: {self.stats['anomalies_detected']}")
        print(f"⚠️  Avertissements: {self.stats['warnings']}")
        print(f"❌ Erreurs: {self.stats['errors']}")
        
        if self.stats['lines_processed'] > 0:
            anomaly_rate = (self.stats['anomalies_detected'] / self.stats['lines_processed']) * 100
            print(f"📊 Taux d'anomalies: {anomaly_rate:.2f}%")
            
            if self.stats['files_processed'] > 0:
                lines_per_file = self.stats['lines_processed'] / self.stats['files_processed']
                print(f"📈 Lignes par fichier: {lines_per_file:.1f}")
        
        if full:
            print("="*60)

# ============================================================================
# FONCTION PRINCIPALE
# ============================================================================

def main():
    """Point d'entrée principal"""
    parser = argparse.ArgumentParser(
        description="Microservice de surveillance et traitement automatique de logs",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Exemples d'utilisation:
  %(prog)s                         # Mode par défaut (IA directe)
  %(prog)s --mode api              # Utiliser l'API Flask
  %(prog)s --mode simulation       # Mode simulation sans IA
  %(prog)s --model mistral         # Utiliser le modèle Mistral
  %(prog)s --interval 5           # Vérifier toutes les 5 secondes
  %(prog)s --delete               # Supprimer les fichiers après traitement
  %(prog)s --help                 # Afficher cette aide

Fonctionnalités:
  • Détection automatique des nouveaux fichiers
  • Analyse IA des logs (via Ollama ou API)
  • Archivage intelligent avec noms uniques
  • Gestion des erreurs et retry automatique
  • Statistiques en temps réel
  • Support multi-encodage (UTF-8, UTF-16, etc.)
        """
    )
    
    parser.add_argument("--mode", choices=["direct", "api", "simulation"], 
                       default="direct", help="Mode de traitement (défaut: direct)")
    parser.add_argument("--model", default="llama3:latest",
                       help="Modèle Ollama à utiliser (défaut: llama3:latest)")
    parser.add_argument("--interval", type=int, default=2,
                       help="Intervalle de vérification en secondes (défaut: 2)")
    parser.add_argument("--log-dir", default="logs",
                       help="Dossier des logs (défaut: logs)")
    parser.add_argument("--archive-dir", default="archive",
                       help="Dossier d'archivage (défaut: archive)")
    parser.add_argument("--results-dir", default="results",
                       help="Dossier des résultats (défaut: results)")
    parser.add_argument("--delete", action="store_true",
                       help="Supprimer les fichiers après traitement (au lieu d'archiver)")
    parser.add_argument("--keep-on-failure", action="store_true",
                       help="Garder les fichiers en cas d'échec d'archivage")
    parser.add_argument("--max-size", type=int, default=10,
                       help="Taille max des fichiers en MB (défaut: 10)")
    parser.add_argument("--verbose", "-v", action="store_true",
                       help="Mode verbeux (niveau DEBUG)")
    parser.add_argument("--quiet", "-q", action="store_true",
                       help="Mode silencieux (niveau WARNING seulement)")
    
    args = parser.parse_args()
    
    # Configuration
    config = {
        "log_dir": args.log_dir,
        "archive_dir": args.archive_dir,
        "results_dir": args.results_dir,
        "check_interval": args.interval,
        "max_file_size_mb": args.max_size,
        "enable_ai": args.mode != "simulation",
        "use_api": args.mode == "api",
        "ai_model": args.model,
        "delete_after_processing": args.delete,
        "delete_on_archive_failure": not args.keep_on_failure
    }
    
    # Niveau de log
    if args.verbose:
        logger.setLevel(logging.DEBUG)
    elif args.quiet:
        logger.setLevel(logging.WARNING)
    
    # Créer et lancer le monitor
    try:
        monitor = LogMonitor(config)
        monitor.monitor()
    except KeyboardInterrupt:
        print("\n\n👋 Arrêt demandé par l'utilisateur")
        sys.exit(0)
    except Exception as e:
        logger.critical(f"Erreur critique: {e}", exc_info=True)
        print(f"\n❌ ERREUR CRITIQUE: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()