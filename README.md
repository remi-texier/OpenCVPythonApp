# Documentation : Intégration de votre propre traitement d'image dans l'application

Cette documentation explique comment modifier le code Python pour intégrer votre propre traitement d'image dans l'application Android. Elle montre aussi comment utiliser les boutons et le slider pour interagir avec le code Python.

## Structure du Code

Le code est divisé en plusieurs fichiers avec des responsabilités spécifiques. Voici les fichiers principaux à modifier pour intégrer le traitement d'image :

- `processing.py` : Contient la logique de traitement d'image.
- `main.py` : Ajoute une gestion de l'interaction entre l'application Android et les scripts Python.

### 1. Modifier le Traitement d'Image

Pour intégrer votre propre traitement d'image, vous devez modifier la fonction `process_image_fun` dans `processing.py`.

#### Étapes pour Modifier le Traitement d'Image

1. **Ouvrir `processing.py`** : Trouvez la fonction `process_image_fun`.
2. **Ajouter votre traitement** : Remplacez ou ajoutez votre code de traitement d'image dans cette fonction.

Exemple de modification :
```python
# processing.py
import numpy as np
import cv2

current_features = 1000

# Fonction de traitement d'image utilisant OpenCV
def process_image_fun(src: np.ndarray) -> np.ndarray:
    global current_features
    try:
        # Exemple : Convertir l'image en niveaux de gris
        gray = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
        
        # Votre propre traitement d'image
        # Ajoutez ici votre code de traitement d'image
        
        return gray
    except Exception as e:
        print(f"Error in process_image_fun: {e}")
        raise
```

### 2. Utiliser les Boutons et le Curseur

Les boutons et le curseur sont configurés pour appeler des fonctions Python spécifiques. Vous pouvez modifier ces fonctions pour qu'elles effectuent des actions personnalisées.

#### Modifier les Actions des Boutons

Les fonctions associées aux boutons sont `button_1_action` et `button_2_action` dans `processing.py`.

1. **Ouvrir `processing.py`**.
2. **Modifier les fonctions de boutons**.

Exemple :
```python
# processing.py

def button_1_action():
    # Action personnalisée pour le bouton 1
    return "Action personnalisée pour le bouton 1"

def button_2_action():
    # Action personnalisée pour le bouton 2
    return "Action personnalisée pour le bouton 2"
```

#### Modifier l'Action du Curseur

La fonction associée au curseur est `slider_change` dans `processing.py`. Cette fonction reçoit la valeur actuelle du curseur et peut être utilisée pour ajuster les paramètres de votre traitement d'image.

1. **Ouvrir `processing.py`**.
2. **Modifier la fonction de curseur**.

Exemple :
```python
# processing.py

def slider_change(value):
    global current_features
    # Utilisez la valeur du curseur pour ajuster un paramètre
    current_features = int(value * 200)
    return f"Valeur du curseur: {value}"
```

### 3. Interaction avec le Traitement d'Image

Les images capturées par la caméra sont envoyées à la fonction `process_image` dans `main.py` qui appelle ensuite votre fonction `process_image_fun`.

#### Modifier `main.py` si Nécessaire

En général, vous n'avez pas besoin de modifier `main.py`, mais si vous avez des besoins spécifiques, vous pouvez ajuster la fonction `process_image`.

Exemple de modification pour enregistrer le temps de traitement :
```python
# main.py

def process_image(image_data: bytes) -> bytes:
    try:
        height, width = 480, 640
        src = convert_to_opencv(image_data, height, width)
        processed_img = process_image_fun(src)
        return convert_to_bytes(processed_img)
    except Exception as e:
        print(f"Error in process_image: {e}")
        raise
```
