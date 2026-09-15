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
- ✅ **Le build CI compile** assembleDebug + assembleRelease (tous les steps verts sur `main`)

## Corrections appliquées récemment

### 1. Signature StorageService.unpack — corrigée (commits `af71627` → `cd718c4`)
L'API réelle de vosk-android est `void unpack(Context, String sourcePath, String targetPath, Callback<Model>, Callback<IOException>)`. Les 7 essais-erreurs précédents ont été nécessaires pour stabiliser l'appel Kotlin avec les types SAM explicites.

### 2. Latch non relâché dans VoskTranscriber — corrigé (commit `4c3b21f`)
Le `CountDownLatch` n'était `countDown()` que dans le callback d'erreur et jamais dans le callback de succès, provoquant une attente systématique de 30s avant chaque démarrage de transcription. Corrigé : appel de `latch.countDown()` dans les deux callbacks.

### 3. Endianness PCM → AAC — corrigé (commit `1376fc2`)
Les buffers `ByteBuffer.wrap()` pour la conversion Short → bytes vers l'encodeur AAC utilisaient `BIG_ENDIAN` par défaut, mais `MediaCodec` attend du `LITTLE_ENDIAN` — chaque échantillon 16-bit avait ses deux octets inversés, produisant un grésillement/statique très fort à la lecture. Corrigé : ajout de `.order(ByteOrder.LITTLE_ENDIAN)` sur les deux buffers concernés (`pcmStagingBuffer` et le buffer dans `enqueueToFeedPool`).

### 4. Fichier `uuid` manquant dans le modèle Vosk — corrigé
`StorageService.unpack()` (vosk-android) lit obligatoirement `assets/<modèle>/uuid` pour détecter les mises à jour du modèle. Le modèle téléchargé sur alphacephei.com ne contient **pas** ce fichier : son absence provoque une `FileNotFoundException` avalée par le `catch (IOException)`, qui affichait à tort « Modèle Vosk introuvable dans assets/... » alors que le modèle était bien présent. Corrigé par :
- création de `app/src/main/assets/vosk-model-small-fr-0.22/uuid` ;
- une tâche Gradle `generateVoskUuid` (racine du module `app`) qui recrée ce fichier automatiquement au build s'il est absent — même approche que le module `:models` officiel de vosk-android ;
- le message d'erreur runtime affiche désormais la cause réelle de l'`IOException` au lieu du texte générique trompeur.

## Étape manuelle obligatoire avant déploiement
- [ ] Télécharger un modèle Vosk français (ex. vosk-model-small-fr-0.22, ~40 Mo) depuis https://alphacephei.com/vosk/models
- [ ] Dézipper et placer le dossier dans `app/src/main/assets/vosk-model-small-fr-0.22/`
- [x] Le fichier `uuid` requis par `StorageService.unpack()` est désormais généré automatiquement au build (tâche Gradle `generateVoskUuid`)
- [ ] Si l'APK est construit par la CI GitHub, **committer le dossier du modèle** : `app/src/main/assets/` n'est pas suivi par git, donc un build CI ne contient aucun modèle

## Contraintes & Pièges identifiés
- **Onglet Appels** : supprimé — l'enregistrement fiable des appels par une app tierce n'est pas praticable sur Android
- **Onglet Applications** : remplacé par le système de catégories
- **Découpe SAF** : openFileDescriptor reste ouvert jusqu'au release() du muxer ; usage normal OK mais usage intensif nécessiterait fermeture explicite du PFD
- **Vosk** : qualité de transcription dépend du modèle choisi (small = rapide mais moins précis)
- **ByteBuffer sur Android** : `ByteBuffer.wrap()` et `ByteBuffer.allocate()` utilisent `BIG_ENDIAN` par défaut, mais le PCM natif Android et `MediaCodec` AAC sont en `LITTLE_ENDIAN` — toujours appeler `.order(ByteOrder.LITTLE_ENDIAN)` explicitement sur tout buffer PCM
- **Lambdas SAM Java/Kotlin** : ne pas omettre les types explicites des paramètres avec `Callback<R>` de vosk-android, sous peine d'erreurs "No value passed for parameter" trompeuses
- **Aucun modèle Vosk dans le dépôt** : le build compile mais la transcription affiche une erreur explicite au runtime