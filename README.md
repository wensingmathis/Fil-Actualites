# Fil — L'actualité sans bruit

Fil est une application Android pensée pour une seule chose : lire les infos simplement, clairement et sans friction.

Plus de publicités agressives, plus de comptes à créer, plus de tracking. Vous choisissez vos sources, vous lisez ce qui compte, et vous gardez le contrôle sur votre expérience.

## Pourquoi choisir Fil ?

- Une lecture simple et lisible
- Sans publicité ni recommandations intrusives
- Sans compte utilisateur
- Sans surveillance inutile
- Compatible avec les flux RSS/Atom
- Fonctionne en français, avec un design net et épuré

## Ce que vous pouvez faire avec Fil

- Suivre les actualités de vos sources préférées
- Ajouter n'importe quel flux RSS ou Atom en HTTPS
- Lire les titres, résumés et dates de publication rapidement
- Ouvrir les articles complets dans le navigateur de votre téléphone
- Relire les 200 derniers titres consultés sans connexion
- Choisir entre un thème clair ou sombre
- Profiter d'une interface minimaliste, rapide et sans distractions

## Une alternative crédible à l'actualité traditionnelle

Fil n'est pas une plateforme d'actualité qui vous pousse vers des contenus sponsorisés. C'est un lecteur RSS indépendant et respectueux de votre attention.

Vous décidez :
- quelles sources suivre ;
- quels sujets suivre ;
- combien d'actualités consulter ;
- comment lire sans être bombardé de publicité.

## Fonctionnalités principales

- Fil d'actualité simple : source, date, titre, résumé
- Sélection de sources comme Franceinfo, Le Monde et Service-Public
- Ajout de flux RSS/Atom personnalisés via Sources → + Ajouter un flux RSS
- Actualisation manuelle via Actualiser
- Sauvegarde locale des 200 derniers titres consultés
- Interface entièrement en français
- Mode clair et mode sombre selon le réglage Android
- Ouverture des articles complets dans le navigateur
- Aucune pub, aucun SDK publicitaire, aucune analytique, aucun compte utilisateur
- Aucun serveur propriétaire ni collecte de données inutiles

## Android pris en charge

Android 8.0 ou supérieur (API 26+), y compris les téléphones Samsung Galaxy A26 5G.

## Obtenir l'APK dans Android Studio

1. Ouvrir ce dossier `Fil-Actualites` dans Android Studio.
2. Laisser Android Studio récupérer le SDK Android 35 et les dépendances Gradle nécessaires. Utiliser Java 17 et Gradle 8.9.
3. Dans le terminal d'Android Studio, exécuter : `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug`
4. Récupérer le fichier `app/build/outputs/apk/debug/app-debug.apk`.
5. Envoyer le fichier sur le téléphone et l'ouvrir pour l'installer.

## Obtenir l'APK sans Android Studio

Le projet contient un workflow GitHub Actions qui compile automatiquement l'APK à chaque push vers `main`.

1. Ouvrir l'onglet Actions du dépôt.
2. Choisir Construire APK Android.
3. Télécharger l'artefact finalisé.
4. Installer le fichier APK sur votre téléphone.

## Confidentialité et sécurité

- L'application demande uniquement la permission `INTERNET`.
- Les flux RSS sont téléchargés directement en HTTPS depuis les sites choisis.
- Les URL non sécurisées et les hôtes douteux sont refusés.
- Aucune donnée de localisation, mot de passe ou identifiant publicitaire n'est demandé.
- Les sources et la liste des articles restent stockées uniquement sur le téléphone.
- Aucun traitement JavaScript distant n'est exécuté dans l'application.

## Tech

- Java 17
- API Android native
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- compileSdk 35
- minSdk 26
- RSS 2.0 et Atom
- XML parsing via XmlPullParser

## Licence du code

Le code du projet est distribué sous licence MIT. Voir [LICENSE](LICENSE).

## Licence de la police

Les fichiers `app/src/main/assets/Arimo-Regular.ttf` et `Arimo-Bold.ttf` viennent du paquet libre `fonts-croscore`, sous licence SIL Open Font License 1.1.

Fil est une solution simple, utile et respectueuse de votre temps. Une application conçue pour lire l'actualité sans détour, sans publicité, sans dérangement et sans dépendre d'un grand système propriétaire.
