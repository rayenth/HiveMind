"""
API REST Flask pour la détection d'anomalies réseau avec IA
Microservice principal du projet HiveMind - Module IA
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import os
import logging
from datetime import datetime
from typing import Dict, List, Optional

# Import local
from detect_anomaly import detect_anomaly, analyze_log_file, DEFAULT_MODEL

# ============================================================================
# CONFIGURATION
# ============================================================================

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('api.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Initialisation de Flask
app = Flask(__name__)
app.config['JSON_SORT_KEYS'] = False
app.config['JSON_AS_ASCII'] = False  # Support des accents

# Activation CORS (Cross-Origin Resource Sharing)
CORS(app, resources={
    r"/*": {
        "origins": ["http://localhost:*", "http://127.0.0.1:*", "*"],
        "methods": ["GET", "POST", "OPTIONS"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})

# ============================================================================
# ROUTES DE L'API
# ============================================================================

@app.route("/", methods=["GET"])
def index():
    """
    Page d'accueil avec documentation de l'API
    ---
    tags:
      - Documentation
    responses:
      200:
        description: Documentation de l'API
    """
    return jsonify({
        "service": "HiveMind - AI Anomaly Detection API",
        "version": "2.0.0",
        "description": "Microservice IA pour la détection d'anomalies dans les logs réseau",
        "documentation": "https://github.com/ton-equipe/hivemind-ai",
        "author": "Équipe HiveMind",
        "endpoints": {
            "GET /": "Cette documentation",
            "POST /api/v1/analyze": "Analyser un log unique",
            "POST /api/v1/analyze/batch": "Analyser plusieurs logs",
            "POST /api/v1/analyze/file": "Analyser un fichier log",
            "GET /api/v1/models": "Liste des modèles disponibles",
            "GET /api/v1/health": "Vérification de l'état du service",
            "GET /api/v1/status": "Statut complet du système"
        },
        "quick_start": {
            "curl_example": """curl -X POST http://localhost:5001/api/v1/analyze \\
  -H "Content-Type: application/json" \\
  -d '{
    "log": "Port scan detected from 192.168.1.100",
    "model": "llama3:latest"
  }'""",
            "python_example": """import requests

response = requests.post(
    "http://localhost:5001/api/v1/analyze",
    json={"log": "Firewall blocked suspicious connection"}
)
print(response.json())"""
        }
    })

@app.route("/api/v1/analyze", methods=["POST"])
def analyze_single():
    """
    Analyse un log unique
    ---
    tags:
      - Analyse
    parameters:
      - in: body
        name: body
        required: true
        schema:
          type: object
          properties:
            log:
              type: string
              description: Le log à analyser
              example: "Port scan detected from 192.168.1.100"
            model:
              type: string
              description: Modèle Ollama à utiliser
              example: "llama3:latest"
    responses:
      200:
        description: Analyse réussie
      400:
        description: Requête invalide
      500:
        description: Erreur interne
    """
    try:
        # Récupération et validation des données
        data = request.get_json()
        
        if not data:
            logger.warning("Requête sans données JSON")
            return jsonify({
                "success": False,
                "error": "Données JSON requises",
                "code": "NO_DATA"
            }), 400
        
        log_text = data.get("log", "").strip()
        model = data.get("model", DEFAULT_MODEL)
        
        if not log_text:
            logger.warning("Log vide reçu")
            return jsonify({
                "success": False,
                "error": "Le champ 'log' ne peut pas être vide",
                "code": "EMPTY_LOG"
            }), 400
        
        # Journalisation
        logger.info(f"Analyse demandée - Modèle: {model}, Log: {log_text[:80]}...")
        
        # Appel à l'IA
        analysis_result = detect_anomaly(log_text, model)
        
        # Construction de la réponse
        response = {
            "success": True,
            "data": {
                "log": log_text,
                "analysis": analysis_result,
                "timestamp": datetime.now().isoformat(),
                "metadata": {
                    "model_used": model,
                    "processing_time_ms": "calculé_si_disponible"
                }
            }
        }
        
        logger.info(f"Analyse terminée - Anomalie: {analysis_result.get('anomaly', False)}")
        return jsonify(response), 200
        
    except Exception as e:
        logger.error(f"Erreur dans /analyze: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": "Erreur interne du serveur",
            "details": str(e),
            "code": "INTERNAL_ERROR",
            "timestamp": datetime.now().isoformat()
        }), 500

@app.route("/api/v1/analyze/batch", methods=["POST"])
def analyze_batch():
    """
    Analyse plusieurs logs en une seule requête
    ---
    tags:
      - Analyse
    parameters:
      - in: body
        name: body
        required: true
        schema:
          type: object
          properties:
            logs:
              type: array
              items:
                type: string
              description: Liste des logs à analyser
            model:
              type: string
              description: Modèle Ollama à utiliser
    responses:
      200:
        description: Analyse par lot réussie
      400:
        description: Requête invalide
      413:
        description: Trop de logs dans la requête
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                "success": False,
                "error": "Données JSON requises",
                "code": "NO_DATA"
            }), 400
        
        logs = data.get("logs", [])
        model = data.get("model", DEFAULT_MODEL)
        
        # Validation
        if not isinstance(logs, list):
            return jsonify({
                "success": False,
                "error": "Le champ 'logs' doit être un tableau",
                "code": "INVALID_FORMAT"
            }), 400
        
        if not logs:
            return jsonify({
                "success": False,
                "error": "Le tableau 'logs' ne peut pas être vide",
                "code": "EMPTY_BATCH"
            }), 400
        
        # Limitation pour éviter les surcharges
        MAX_BATCH_SIZE = 100
        if len(logs) > MAX_BATCH_SIZE:
            logger.warning(f"Batch trop grand: {len(logs)} logs, limité à {MAX_BATCH_SIZE}")
            logs = logs[:MAX_BATCH_SIZE]
        
        logger.info(f"Début de l'analyse par lot - {len(logs)} logs avec le modèle {model}")
        
        # Analyse de chaque log
        results = []
        anomalies_count = 0
        
        for index, log_text in enumerate(logs):
            if log_text and isinstance(log_text, str):
                log_text = log_text.strip()
                if log_text:
                    result = detect_anomaly(log_text, model)
                    result["original_log"] = log_text
                    result["index"] = index
                    results.append(result)
                    
                    if result.get("anomaly", False):
                        anomalies_count += 1
        
        # Compilation des statistiques
        stats = {
            "total_logs": len(logs),
            "processed_logs": len(results),
            "anomalies_detected": anomalies_count,
            "anomaly_percentage": (anomalies_count / len(results) * 100) if results else 0
        }
        
        response = {
            "success": True,
            "data": {
                "statistics": stats,
                "results": results,
                "timestamp": datetime.now().isoformat(),
                "model_used": model
            }
        }
        
        logger.info(f"Analyse par lot terminée - {anomalies_count} anomalies détectées")
        return jsonify(response), 200
        
    except Exception as e:
        logger.error(f"Erreur dans /analyze/batch: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": "Erreur lors du traitement par lot",
            "details": str(e),
            "timestamp": datetime.now().isoformat()
        }), 500

@app.route("/api/v1/analyze/file", methods=["POST"])
def analyze_file():
    """
    Analyse un fichier log complet
    ---
    tags:
      - Fichiers
    consumes:
      - multipart/form-data
    parameters:
      - in: formData
        name: file
        type: file
        required: true
        description: Fichier log à analyser
      - in: formData
        name: model
        type: string
        required: false
        description: Modèle Ollama à utiliser
    responses:
      200:
        description: Fichier analysé avec succès
      400:
        description: Aucun fichier fourni
    """
    try:
        if 'file' not in request.files:
            return jsonify({
                "success": False,
                "error": "Aucun fichier fourni",
                "code": "NO_FILE"
            }), 400
        
        file = request.files['file']
        model = request.form.get('model', DEFAULT_MODEL)
        
        if file.filename == '':
            return jsonify({
                "success": False,
                "error": "Nom de fichier vide",
                "code": "EMPTY_FILENAME"
            }), 400
        
        # Sauvegarde temporaire du fichier
        import tempfile
        with tempfile.NamedTemporaryFile(delete=False, suffix='.log') as tmp_file:
            file.save(tmp_file.name)
            tmp_path = tmp_file.name
        
        try:
            logger.info(f"Analyse du fichier: {file.filename} avec le modèle {model}")
            
            # Analyse du fichier
            result = analyze_log_file(tmp_path, model)
            result["filename"] = file.filename
            
            # Nettoyage
            os.unlink(tmp_path)
            
            return jsonify({
                "success": True,
                "data": result
            }), 200
            
        except Exception as e:
            # Nettoyage en cas d'erreur
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
            raise e
            
    except Exception as e:
        logger.error(f"Erreur dans /analyze/file: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": "Erreur lors de l'analyse du fichier",
            "details": str(e),
            "timestamp": datetime.now().isoformat()
        }), 500

@app.route("/api/v1/models", methods=["GET"])
def list_models():
    """
    Liste les modèles Ollama disponibles
    ---
    tags:
      - Système
    responses:
      200:
        description: Liste des modèles disponibles
      500:
        description: Erreur de connexion à Ollama
    """
    try:
        from ollama import list as list_ollama_models
        
        models_response = list_ollama_models()
        models = [model['name'] for model in models_response.get('models', [])]
        
        return jsonify({
            "success": True,
            "data": {
                "available_models": models,
                "default_model": DEFAULT_MODEL,
                "total_models": len(models),
                "recommended_models": ["llama3:latest", "mistral", "gemma:2b"]
            }
        }), 200
        
    except Exception as e:
        logger.error(f"Erreur lors de la liste des modèles: {str(e)}")
        return jsonify({
            "success": False,
            "error": "Impossible de récupérer la liste des modèles",
            "details": str(e),
            "suggestion": "Vérifiez qu'Ollama est installé et en cours d'exécution"
        }), 500

@app.route("/api/v1/health", methods=["GET"])
def health_check():
    """
    Vérification de l'état du service
    ---
    tags:
      - Monitoring
    responses:
      200:
        description: Service en bonne santé
    """
    try:
        # Test de base du module IA
        test_result = detect_anomaly("Health check test", DEFAULT_MODEL)
        
        return jsonify({
            "success": True,
            "status": "healthy",
            "timestamp": datetime.now().isoformat(),
            "checks": {
                "api": "operational",
                "ai_module": "connected" if test_result else "degraded",
                "ollama": "available"
            },
            "version": "2.0.0",
            "uptime": "calculé_si_disponible"
        }), 200
        
    except Exception as e:
        return jsonify({
            "success": False,
            "status": "unhealthy",
            "error": str(e),
            "timestamp": datetime.now().isoformat()
        }), 500

@app.route("/api/v1/status", methods=["GET"])
def system_status():
    """
    Statut complet du système
    ---
    tags:
      - Monitoring
    responses:
      200:
        description: Statut du système
    """
    import sys
    import platform
    
    try:
        from ollama import list as list_ollama_models
        models = list_ollama_models()
        ollama_status = "connected"
        available_models = len(models.get('models', []))
    except Exception as e:
        ollama_status = "disconnected"
        available_models = 0
    
    return jsonify({
        "success": True,
        "data": {
            "system": {
                "python_version": sys.version,
                "platform": platform.platform(),
                "hostname": platform.node(),
                "processor": platform.processor()
            },
            "service": {
                "name": "HiveMind AI Anomaly Detection",
                "version": "2.0.0",
                "status": "running",
                "port": 5001,
                "environment": os.getenv("FLASK_ENV", "development")
            },
            "ai": {
                "ollama_status": ollama_status,
                "available_models": available_models,
                "default_model": DEFAULT_MODEL,
                "last_check": datetime.now().isoformat()
            },
            "resources": {
                "working_directory": os.getcwd(),
                "log_directory": "logs" if os.path.exists("logs") else "not_created",
                "archive_directory": "archive" if os.path.exists("archive") else "not_created"
            }
        }
    }), 200

# ============================================================================
# GESTIONNAIRES D'ERREURS
# ============================================================================

@app.errorhandler(404)
def not_found(error):
    return jsonify({
        "success": False,
        "error": "Endpoint non trouvé",
        "message": "Consultez la documentation à GET /",
        "code": "NOT_FOUND"
    }), 404

@app.errorhandler(405)
def method_not_allowed(error):
    return jsonify({
        "success": False,
        "error": "Méthode non autorisée",
        "code": "METHOD_NOT_ALLOWED"
    }), 405

@app.errorhandler(500)
def internal_error(error):
    logger.error(f"Erreur interne: {error}")
    return jsonify({
        "success": False,
        "error": "Erreur interne du serveur",
        "code": "INTERNAL_SERVER_ERROR",
        "timestamp": datetime.now().isoformat()
    }), 500

# ============================================================================
# POINT D'ENTRÉE PRINCIPAL
# ============================================================================

if __name__ == "__main__":
    # Configuration
    PORT = int(os.getenv("PORT", 5001))
    HOST = os.getenv("HOST", "0.0.0.0")
    DEBUG = os.getenv("FLASK_ENV", "development") == "development"
    
    # Message de démarrage
    print("\n" + "="*60)
    print("🚀 HIVEMIND - AI ANOMALY DETECTION API")
    print("="*60)
    print(f"📡 URL: http://{HOST}:{PORT}")
    print(f"📚 Documentation: http://{HOST}:{PORT}/")
    print(f"⚙️  Mode: {'DEVELOPPEMENT' if DEBUG else 'PRODUCTION'}")
    print(f"🤖 Modèle par défaut: {DEFAULT_MODEL}")
    print("\n📋 Endpoints principaux:")
    print("   GET  /                     - Documentation")
    print("   POST /api/v1/analyze       - Analyser un log unique")
    print("   POST /api/v1/analyze/batch - Analyser plusieurs logs")
    print("   POST /api/v1/analyze/file  - Analyser un fichier")
    print("   GET  /api/v1/models        - Modèles disponibles")
    print("   GET  /api/v1/health        - Health check")
    print("   GET  /api/v1/status        - Statut système")
    print("="*60 + "\n")
    
    # Démarrer le serveur
    try:
        app.run(
            host=HOST,
            port=PORT,
            debug=DEBUG,
            threaded=True  # Support des requêtes concurrentes
        )
    except KeyboardInterrupt:
        print("\n\n👋 Arrêt du serveur...")
    except Exception as e:
        print(f"\n❌ Erreur lors du démarrage: {e}")
        logger.critical(f"Erreur critique au démarrage: {e}", exc_info=True)