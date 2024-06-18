import numpy as np
import cv2

current_features = 1000

# Fonction de traitement d'image utilisant OpenCV
def process_image_fun(src: np.ndarray) -> np.ndarray:
    global current_features
    try:
        blurred = cv2.GaussianBlur(src, (21, 21), 0)
        orb = cv2.ORB_create(current_features)
        keyPts, _ = orb.detectAndCompute(blurred, None)
        outimg = cv2.drawKeypoints(src, keyPts, None, color=(0, 255, 0), flags=0)
        return outimg
    except Exception as e:
        print(f"Error in process_image_opencv: {e}")
        raise

# Action associée au bouton 1
def button_1_action():
    return "Button 1 pressed"

# Action associée au bouton 2
def button_2_action():
    return "Button 2 pressed"

# Action associée au changement de slider
def slider_change(value):
    global current_features
    current_features = int(value * 200)
    return f"Slider value: {value}"