# État du Projet & Prochaines Actions

## Validations techniques récentes
- [x] Structure de base créée
- [x] Migration de SpeechRecognizer vers Vosk (offline, modèle français)
- [x] Ajout VoskTranscriber.kt — transcription offline via vosk-android:0.3.47
- [x] AudioRecordEngine étendu avec PcmListener pour alimenter Vosk en temps réel
- [x] Suppression TranscriptionManager.kt (obsolète, basé sur SpeechRecognizer)
- [x] Système de catégories (Réunion / Note vocale / Favori) via fichier sidecar .tag
- [x] Filtres de catégories dans l'UI (remplace les onglets Appels/Applications)
- [x] RecordingCard enrichie avec menu ⋮ de catégorisation + badge catégorie
- [x] Découpe audio vers dossier SAF (OpenDocumentTree + MediaMuxer sur ParcelFileDescriptor)
- [x] Sélection sortie audio (haut-parleur/écouteur) via bouton casque
- [x] Build CI GitHub Actions configuré (debug + release APK)

## Build CI
- Dépôt : https://github.com/SpasfonK/ObsidianRECORDER
- Workflow : Android CI Build (déclenché sur push main, PR, et workflow_dispatch)
- Dernier run : https://github.com/SpasfonK/ObsidianRECORDER/actions
- Correction en cours : adaptation API StorageService.unpack de vosk-android

## Étape manuelle obligatoire avant déploiement
- [ ] Télécharger un modèle Vosk français (ex. vosk-model-small-fr-0.22, ~40 Mo) depuis https://alphacephei.com/vosk/models
- [ ] Dézipper et placer le dossier dans `app/src/main/assets/vosk-model-small-fr-0.22/`

## Contraintes & Pièges identifiés
- **Onglet Appels** : supprimé — l'enregistrement fiable des appels par une app tierce n'est pas praticable sur Android
- **Onglet Applications** : remplacé par le système de catégories
- **Découpe SAF** : openFileDescriptor reste ouvert jusqu'au release() du muxer ; usage normal OK mais usage intensif nécessiterait fermeture explicite du PFD
- **Vosk** : qualité de transcription dépend du modèle choisi (small = rapide mais moins précis)
- **StorageService.unpack** : API asynchrone avec callbacks Model/onDone, stabilisation du code en cours via CI
- **Aucun modèle Vosk dans le dépôt** : le build compile mais la transcription affiche une erreur explicite au runtime