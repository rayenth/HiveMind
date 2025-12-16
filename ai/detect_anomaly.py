"""
Module de détection d'anomalies IA
Utilise Ollama pour analyser des logs réseau et détecter des comportements suspects
"""

import json
import re
import os
import logging
from typing import Dict, List, Optional, Union
from ollama import chat
from datetime import datetime

# Configuration du logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# ============================================================================
# CONSTANTES DE CONFIGURATION
# ============================================================================

DEFAULT_MODEL = "llama3:latest"  # Modèle par défaut
LOGS_DIR = "logs"                # Dossier des logs à analyser
ARCHIVE_DIR = "archive"          # Dossier d'archivage
SUPPORTED_ENCODINGS = ['utf-8', 'cp1252', 'latin-1', 'iso-8859-1']

# ============================================================================
# FONCTION PRINCIPALE DE DÉTECTION
# ============================================================================

def detect_anomaly(log_text: str, model: str = DEFAULT_MODEL) -> Dict:
    """
    Analyse un log réseau avec un modèle IA pour détecter des anomalies
    
    Args:
        log_text (str): Le message de log à analyser
        model (str): Nom du modèle Ollama à utiliser (par défaut: "llama3:latest")
    
    Returns:
        Dict: Résultat d'analyse au format JSON contenant:
            - anomaly (bool): True si anomalie détectée
            - confidence (float): Niveau de confiance (0.0 à 1.0)
            - reason (str): Explication en français
            - category (str): Type d'anomalie
            - model_used (str): Modèle utilisé pour l'analyse
    
    Example:
        >>> result = detect_anomaly("Port scan detected from 192.168.1.100")
        >>> print(result)
        {
            "anomaly": true,
            "confidence": 0.92,
            "reason": "Scan de ports détecté, comportement suspect",
            "category": "network_scan",
            "model_used": "llama3:latest"
        }
    """
    
    # Construction du prompt pour le modèle IA
    prompt = f"""
    Tu es un expert en sécurité réseau. Analyse ce log et détermine s'il s'agit d'une anomalie.
    
    CONTEXTE:
    - Un log normal: connexions réussies, requêtes HTTP 200, activités autorisées
    - Une anomalie: scans de ports, attaques DDoS, tentatives de bruteforce, accès non autorisés
    
    LOG À ANALYSER: "{log_text}"
    
    FORMAT DE RÉPONSE OBLIGATOIRE (JSON uniquement):
    {{
        "anomaly": true ou false,
        "confidence": un nombre entre 0.0 et 1.0,
        "reason": "explication courte en français",
        "category": "type d'anomalie (si applicable)"
    }}
    
    Réponds uniquement avec le JSON, sans commentaires supplémentaires.
    """
    
    try:
        logger.debug(f"Analyse du log avec le modèle '{model}': {log_text[:50]}...")
        
        # Appel au modèle IA via Ollama
        response = chat(
            model=model,
            messages=[{'role': 'user', 'content': prompt}],
            options={'temperature': 0.1}  # Faible température pour des réponses cohérentes
        )
        
        # Extraction de la réponse
        response_text = response['message']['content']
        
        # Recherche du JSON dans la réponse
        json_match = re.search(r'\{.*\}', response_text, re.DOTALL)
        
        if json_match:
            # Parsing du JSON
            result = json.loads(json_match.group(0))
            
            # Validation et normalisation du résultat
            validated_result = {
                "anomaly": bool(result.get("anomaly", False)),
                "confidence": float(result.get("confidence", 0.5)),
                "reason": str(result.get("reason", "Analyse effectuée")),
                "category": str(result.get("category", "unknown")),
                "model_used": model
            }
            
            # S'assurer que la confiance est dans [0, 1]
            validated_result["confidence"] = max(0.0, min(1.0, validated_result["confidence"]))
            
            logger.info(f"Analyse terminée - Anomalie: {validated_result['anomaly']} (confiance: {validated_result['confidence']:.2f})")
            return validated_result
            
        else:
            # Aucun JSON trouvé dans la réponse
            logger.warning(f"Aucun JSON valide dans la réponse du modèle: {response_text[:100]}...")
            return {
                "anomaly": False,
                "confidence": 0.0,
                "reason": "Format de réponse invalide du modèle IA",
                "category": "parsing_error",
                "model_used": model
            }
            
    except json.JSONDecodeError as e:
        logger.error(f"Erreur de décodage JSON: {e}")
        return {
            "anomaly": False,
            "confidence": 0.0,
            "reason": f"Erreur de format JSON: {str(e)}",
            "category": "json_error",
            "model_used": model
        }
        
    except Exception as e:
        logger.error(f"Erreur lors de l'appel à Ollama: {e}")
        return {
            "anomaly": False,
            "confidence": 0.0,
            "reason": f"Erreur de connexion au modèle IA: {str(e)}",
            "category": "connection_error",
            "model_used": model
        }

# ============================================================================
# FONCTIONS UTILITAIRES POUR LA GESTION DES FICHIERS
# ============================================================================

def ensure_directory(directory_path: str) -> None:
    """Crée un dossier s'il n'existe pas"""
    if not os.path.exists(directory_path):
        os.makedirs(directory_path)
        logger.info(f"Dossier créé: {directory_path}")

def detect_file_encoding(filepath: str) -> str:
    """
    Détecte l'encodage d'un fichier texte
    
    Args:
        filepath (str): Chemin du fichier
    
    Returns:
        str: Encodage détecté (ex: 'utf-8', 'cp1252')
    """
    try:
        import chardet
        with open(filepath, 'rb') as f:
            raw_data = f.read(10000)  # Lire les 10 premiers Ko
            detection = chardet.detect(raw_data)
            return detection.get('encoding', 'utf-8')
    except ImportError:
        logger.warning("Module chardet non installé, utilisation de l'encodage par défaut")
        return 'utf-8'
    except Exception as e:
        logger.error(f"Erreur lors de la détection d'encodage: {e}")
        return 'utf-8'

def read_log_file(filepath: str) -> List[str]:
    """
    Lit un fichier log en gérant automatiquement l'encodage
    
    Args:
        filepath (str): Chemin du fichier log
    
    Returns:
        List[str]: Liste des lignes du fichier
    """
    try:
        # Essayer différents encodages courants
        for encoding in SUPPORTED_ENCODINGS:
            try:
                with open(filepath, 'r', encoding=encoding) as f:
                    lines = [line.strip() for line in f if line.strip()]
                logger.debug(f"Fichier lu avec l'encodage: {encoding}")
                return lines
            except UnicodeDecodeError:
                continue
        
        # Si aucun encodage standard ne fonctionne
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
            lines = [line.strip() for line in f if line.strip()]
        logger.warning(f"Fichier lu avec ignore errors: {filepath}")
        return lines
        
    except Exception as e:
        logger.error(f"Erreur lors de la lecture du fichier {filepath}: {e}")
        return []

# ============================================================================
# FONCTION D'ANALYSE DE FICHIERS COMPLETS
# ============================================================================

def analyze_log_file(filepath: str, model: str = DEFAULT_MODEL) -> Dict:
    """
    Analyse toutes les lignes d'un fichier log
    
    Args:
        filepath (str): Chemin du fichier à analyser
        model (str): Modèle Ollama à utiliser
    
    Returns:
        Dict: Résultats de l'analyse avec statistiques
    """
    logger.info(f"Début de l'analyse du fichier: {filepath}")
    
    # Lire le fichier
    lines = read_log_file(filepath)
    if not lines:
        return {
            "success": False,
            "error": "Fichier vide ou impossible à lire",
            "file": os.path.basename(filepath)
        }
    
    # Analyser chaque ligne
    results = []
    anomalies = []
    
    for i, line in enumerate(lines, 1):
        result = detect_anomaly(line, model)
        result["line_number"] = i
        result["original_log"] = line
        results.append(result)
        
        if result["anomaly"]:
            anomalies.append(result)
        
        # Log progressif
        if i % 10 == 0:
            logger.debug(f"Progression: {i}/{len(lines)} lignes analysées")
    
    # Compilation des statistiques
    stats = {
        "total_lines": len(lines),
        "analyzed_lines": len(results),
        "anomalies_detected": len(anomalies),
        "anomaly_rate": len(anomalies) / len(results) if results else 0,
        "most_common_category": max(
            [r["category"] for r in results if r["anomaly"]],
            key=[r["category"] for r in results if r["anomaly"]].count,
            default="none"
        )
    }
    
    logger.info(f"Analyse terminée: {stats['anomalies_detected']} anomalies détectées sur {stats['total_lines']} lignes")
    
    return {
        "success": True,
        "file": os.path.basename(filepath),
        "statistics": stats,
        "anomalies": anomalies,
        "sample_results": results[:5],  # Retourne les 5 premiers résultats comme échantillon
        "model_used": model,
        "timestamp": datetime.now().isoformat()
    }

# ============================================================================
# FONCTION PRINCIPALE POUR L'ANALYSE EN LIGNE DE COMMANDE
# ============================================================================

def main():
    """Fonction principale pour l'exécution en ligne de commande"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Analyseur de logs réseau avec IA')
    parser.add_argument('--log', type=str, help='Log unique à analyser')
    parser.add_argument('--file', type=str, help='Fichier log à analyser')
    parser.add_argument('--model', type=str, default=DEFAULT_MODEL, 
                       help=f"Modèle Ollama à utiliser (défaut: {DEFAULT_MODEL})")
    parser.add_argument('--list-models', action='store_true', 
                       help='Lister les modèles Ollama disponibles')
    
    args = parser.parse_args()
    
    # Lister les modèles disponibles
    if args.list_models:
        try:
            from ollama import list as list_models
            models = list_models()
            print("📦 Modèles Ollama disponibles:")
            for model in models.get('models', []):
                print(f"  • {model['name']}")
        except Exception as e:
            print(f"❌ Erreur lors de la liste des modèles: {e}")
        return
    
    # Analyser un log unique
    if args.log:
        print(f"🔍 Analyse du log avec le modèle '{args.model}':")
        print(f"   Log: {args.log}")
        result = detect_anomaly(args.log, args.model)
        print(f"📊 Résultat: {json.dumps(result, indent=2, ensure_ascii=False)}")
    
    # Analyser un fichier
    elif args.file:
        if os.path.exists(args.file):
            result = analyze_log_file(args.file, args.model)
            print(json.dumps(result, indent=2, ensure_ascii=False))
        else:
            print(f"❌ Fichier non trouvé: {args.file}")
    
    # Mode interactif
    else:
        print("🤖 Analyseur de logs IA - Mode interactif")
        print("Tapez 'quit' pour quitter")
        print(f"Modèle par défaut: {DEFAULT_MODEL}")
        print("-" * 50)
        
        while True:
            try:
                log_input = input("\n📝 Entrez un log à analyser: ").strip()
                if log_input.lower() in ['quit', 'exit', 'q']:
                    break
                if log_input:
                    result = detect_anomaly(log_input)
                    print(f"📊 Résultat: {json.dumps(result, indent=2, ensure_ascii=False)}")
            except KeyboardInterrupt:
                print("\n\n👋 Au revoir!")
                break
            except Exception as e:
                print(f"❌ Erreur: {e}")

if __name__ == "__main__":
    main()