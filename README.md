# Fil — l'actualité, simplement

Application Android native, minimaliste, sans publicité, sans compte et sans suivi. Fil rassemble les actualités des flux RSS choisis par l'utilisateur.

## Fonctionnalités

- Fil d'actualité simple : source, date, titre, résumé.
- Choix des sources : Franceinfo, Le Monde, Service-Public.
- Ajout de n'importe quel flux RSS/Atom en HTTPS via **Sources → + Ajouter un flux RSS**.
- Actualisation manuelle via **Actualiser**.
- Sauvegarde locale des 200 derniers titres consultés pour les relire sans réseau.
- Interface entièrement en français, avec mode clair ou sombre selon le réglage Android.
- Ouverture des articles complets dans le navigateur du téléphone.
- Aucune publicité, SDK publicitaire, analytique, compte utilisateur ni serveur propriétaire dans l'application.
- Pas d'images ni de recommandations algorithmiques.
- Police **Arimo** (métriquement compatible avec Arial, sous licence libre OFL 1.1). Arial elle-même n'est pas incluse pour des raisons de licence.

**Important :** Fil est un lecteur RSS indépendant, **pas l'application officielle Google Actualités**. Les articles complets s'ouvrent sur les sites des éditeurs, qui peuvent afficher leurs propres publicités, abonnements ou cookies. Certains flux changent ou tombent en panne indépendamment de l'application.

## Android pris en charge

Android 8.0 ou supérieur (API 26+), y compris les téléphones Samsung Galaxy A26 5G.

## Obtenir l'APK dans Android Studio

1. Ouvrir ce dossier `Fil-Actualites` dans **Android Studio**.
2. Laisser Android Studio récupérer le SDK Android 35 et les dépendances Gradle nécessaires. Utiliser **Java 17** et **Gradle 8.9**.
3. Dans le terminal d'Android Studio, exécuter `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug` (ou **Build → Build APK(s)**).
4. Récupérer le fichier `app/build/outputs/apk/debug/app-debug.apk`.
5. Envoyer le fichier sur le téléphone, puis l'ouvrir pour l'installer (autoriser l'installation depuis cette source si Android le demande).

## Obtenir l'APK sans Android Studio : GitHub Actions

Le projet contient un fichier `.github/workflows/apk.yml` qui compile automatiquement l'APK sur les serveurs GitHub à chaque push vers `main`, et peut aussi être lancé manuellement.

1. Ouvrir l'onglet **Actions** du dépôt.
2. Choisir **Construire APK Android**. Pour un lancement manuel, sélectionner **Run workflow** et confirmer la branche `main`.
3. Ouvrir l'exécution terminée, puis télécharger l'artefact **Fil-Actualites-debug**.
4. Décompresser l'archive : elle contient `Fil-Actualites-debug.apk`, à installer sur Android.

Le workflow exécute `testDebugUnitTest` puis assemble l'APK. Le projet ne contient actuellement pas de tests unitaires dédiés; l'étape détectera et exécutera les tests ajoutés ultérieurement.

L'APK `debug` est destiné aux essais personnels. Pour une publication sur une boutique, il faut compiler et signer une version `release` avec une clé de signature privée.

## Vie privée et sécurité

- L'application ne demande que la permission `INTERNET`.
- Les flux RSS sont téléchargés **directement en HTTPS** depuis les sites choisis. Ces sites voient techniquement les requêtes réseau et l'adresse IP habituelle du téléphone.
- Les redirections non HTTPS et les hôtes RSS qui résolvent vers des adresses locales ou privées sont refusés; les URL d'articles doivent également utiliser HTTPS.
- Aucun mot de passe, donnée de localisation ou identifiant publicitaire n'est demandé.
- Les sources choisies et la dernière liste d'articles sont enregistrées uniquement sur le téléphone (SharedPreferences) et ne sont pas sauvegardées dans le cloud de l'application.
- Les flux XML ont une taille maximale de 2 Mo et le traitement des DTD est désactivé.
- Le contenu riche distant n'est jamais exécuté comme du JavaScript dans l'application : seuls les textes des articles sont affichés.

## Tech

- Java 17
- API Android native (aucune bibliothèque d'interface ni SDK publicitaire)
- Gradle 8.9, Android Gradle Plugin 8.7.3, compileSdk 35
- minSdk 26 (Android 8.0)
- RSS 2.0 et Atom, parsing avec XmlPullParser

## Licence de la police

Les fichiers `app/src/main/assets/Arimo-Regular.ttf` et `Arimo-Bold.ttf` viennent du paquet libre `fonts-croscore`. Leur notice de licence est fournie dans `ARIMO-LICENCE.txt` (SIL Open Font License 1.1).

## Licence du code

Le code du projet est distribué sous licence MIT (voir [LICENSE](LICENSE)). La police Arimo reste distribuée sous sa licence OFL 1.1 distincte. Utilisez uniquement des flux publics dont les conditions autorisent la consultation; les articles complets restent hébergés par leurs éditeurs.
