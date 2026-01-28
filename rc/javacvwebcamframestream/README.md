# JavaCVWebcamFrameStream

Ce projet capture des images depuis une caméra et convertit l'image en ByteBuffer. Ce projet est expérimental. Ce projet est une bibliothèque dans le sens où il doit être utilisé comme dépendance.

## Comment compiler et déployer

```
mvn clean install
```

(a bien verif ce que je raconte dessous)

## Hardware

Camera de base : ?ref?

Connectée sur **CSI** du RPI5.

La caméra CSI transmet des données brutes via MIPI CSI-2. Il faut donc utiliser une librairie qui sait communiquer avec les données envoyées (j'imagine que la caméra envoie ses données sur le file descriptor, avec des buffers spéciaux). On peut utiliser la librairie recommandée par Raspberry, en C++, **libcamera**. Elle configure le pipeline et fournit des frames dans un format exploitable (souvent YUV, parfois RGB ou RAW selon config).

Le but du composant C/C++ est de récuperer les fonctions C++, l'adapter pour notre usage (créer la caméra, obtenir une image, détruire la caméra), faire un **wrapper C** pour appeler ces fonctions via **binding Java Panama**. Pour l'obtention d'une image, il faut convertir les données brutes **YUV** dans un format compréhensible, on choisit le **JPEG compressé** par facilité. D'autres formats pourraient être envisagés, MJPEG, voire RTP via WebRTC / GStreamer.

On utilise la librairie **libjpeg** pour convertir en JPEG compressé. Il faut que j'ajuste les valeurs RGB selon les données YUV pour avoir une image potable.

Améliorations : 
- Actuellement, après websocket, on reçoit les images à 30 FPS. On peut utiliser la librairie **turbojpeg** (à installer) pour gagner en fps.
- Wrokflow actuel : Recevoir YUV -> Traiter données (conversion RGB puis JPEG Compressé) -> Envoyer Données Traitées;
On pourrait passer en multi-thread avec Recevoir YUV / traitement de données en parallèle.

Doc libcamera : https://libcamera.org/index.html

Doc turbojpeg : https://github.com/libjpeg-turbo/libjpeg-turbo

Doc panama : https://github.com/openjdk/panama-foreign/blob/foreign-memaccess%2Babi/doc/panama_ffi.md

## Composant camera sur RPI5

### Installation

Suivre l'installation du **rpicam**, **libcamera** pour PI OS Lite (pas d'écran) sur la doc officielle. Ne pas oublier de désinstaller libcamera si déjà installé.

https://www.raspberrypi.com/documentation/computers/camera_software.html#advanced-rpicam-apps

Commande pour voir si caméra detectée : 

```bash
rpicam-hello -n
```

Si problème avec **libpisp**, désinstaller celui existant, installer via github officiel & meson. Réinstaller libcamera avec ce nouveau libpisp.

Github libpisp : https://github.com/raspberrypi/libpisp

### Code C/C++

```bash
sudo apt install libjpeg-dev
```

Installation turbojpeg : https://www.linuxfromscratch.org/blfs/view/svn/general/libjpeg.html











