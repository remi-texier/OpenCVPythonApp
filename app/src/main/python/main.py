import numpy as np
import cv2
from processing import *
import importlib.util
import os

current_features = 1000

# Convertit les données d'image en format OpenCV
def convert_to_opencv(image_data: bytes, height: int, width: int) -> np.ndarray:
    try:
        arr = np.frombuffer(image_data, dtype=np.uint8)
        image = arr.reshape((height, width, 4))
        return image
    except Exception as e:
        print(f"Error in convert_to_opencv: {e}")
        raise

# Convertit l'image OpenCV en bytes
def convert_to_bytes(image: np.ndarray) -> bytes:
    try:
        return image.tobytes()
    except Exception as e:
        print(f"Error in convert_to_bytes: {e}")
        raise

# Fonction de traitement d'image principale
def process_image(image_data: bytes) -> bytes:
    try:
        height, width = 480, 640
        src = convert_to_opencv(image_data, height, width)
        processed_img = process_image_fun(src)
        return convert_to_bytes(processed_img)
    except Exception as e:
        print(f"Error in process_image: {e}")
        raise

# Liste les fichiers dans le dossier partagé
def list_files():
    try:
        return os.listdir(shared_folder_path)
    except Exception as e:
        return str(e)

# Exécute un fichier Python spécifique dans le dossier partagé
def execute_python_file(file_name: str):
    file_path = os.path.join(shared_folder_path, file_name)
    if not os.path.isfile(file_path):
        return f"File not found: {file_name}"

    try:
        spec = importlib.util.spec_from_file_location("module.name", file_path)
        foo = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(foo)
        return f"Executed {file_name} successfully."
    except Exception as e:
        return f"Error executing {file_name}: {str(e)}"