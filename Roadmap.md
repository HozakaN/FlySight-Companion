# Known issues

- Check what's up with connection callback not being called on Nexus 6P (Api 28 ?)

# DONE release/1.0.0 RC1

- DONE Scan devices :
+ ajouter un méchanisme de "increment: Int" dans le LoadingState.Loading
+ premier delay, second et troisième delay increment ce nombre
+ UI : changer le texte en fonction de increment. Au dernier, Indiquer de mettre le FlySight en pairing mode pour aider
+ ajouter un bouton pour cancel le scan

- DONE bug duplicate config sur une update de config

- DONE ajouter un méchanisme d'attente sur les jobs du FlySightDeviceImpl afin de ne pas avoir de jobs concurrents
+ Possibilité de donner une importance supérieure au job de ping. scheduler

- DONE améliorer l'interface de list des devices en :
 + changeant la position du bouton "add device" quand il y a déjà des devices (Icons.Default.NewWindow)
 + ajoutant un spinner dans l'action bar quand un scan est en cours avec des devices déjà trouvés

- DONE Improve config file picking dialog spacing between elements

- DONE Package and app naming

- DONE Possible issue de button de connexion qui fonctionne mal après une déconnexion sur de la navigation user rapide

- TO FINISH LATER FlySight design system

- DONE Logo

- DONE Lower Android api level requirement

# DONE release/1.0.0 RC2

- DONE list devices screen
+ no device
++ revoir le positionnement vertical des items (espacement entre bloc texte et bloc buttons)
+ no device refreshing
++ revoir le positionnement vertical des items (chargement bloc au centre, cancel button aux 3.4)

- DONE mettre des DropDownMenu dans une Box avec l'icon MoreVert correspondant

- DONE Localisation fr

- DONE UI of config file edition

- DONE app versioning indication in-app

- DONE Ability to duplicate a config

- DONE Fix text color on some dialogs/dropdowns

- DONE Hide "File" textfield in Initialization section of config file edition

- DONE for PlayStore app, a Feedback screen, with Name, contact, message and allowing to pass the logs from a devices

# DONE release/1.0.0 RC3

- DONE gradle modules organization and clean up

# DONE release/1.0.0 RC4

- NOPE Help on config file edition and display ?

- DONE Fix textfield issues (0 staying when clearing field)

- DONE Fix FlySight tab logo on devices with density < my phone density

# TODO release/1.0.0 RC5

- Distance is only displayed in meters in the Configuration Card of a device on the list of devices

- Some coroutines are hanging up in FlySightDeviceImpl class

# TODO release/2.0.0 RC1

- DONE big feature : Live GNSS data and HUDs V1

- DONE Reference points

- DONE firmware updater

- DONE FlyBlind

- DONE Many fixes and improvements

# TODO release/2.0.0 RC2

- Check feedback not exporting device logs in playstore app

- Check download of TRACK.CSV files from FlySight file trees

- Update compatibility matrix to expose latests FlySight firmware versions

- Auto launch FlySights when starting a session with a FlySight as source

- Hide content from Record detail screen

# TODO release/2.1.0

- Help on config file edition and display (if not in 2.0.0)

- big feature : record analyses

# TODO release/3.0.0

- big feature : iOS

# TODO release/4.0.0

- big feature : Desktop