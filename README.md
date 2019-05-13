# ct-android
A port of Color Thief for Android

This is a port of https://github.com/lokesh/color-thief Java source to be compatible with Android API Levels 19 and later. Instead of the Java framework's objects, this variant utilizes the Android SDKs structures.

This port has been made with a device's camera input in mind, which means that specific color space identification algorithms from the original codebase were reduced to just one.

## Usage

Just like with the original, you create a color map by passing an Android.Graphics.Bitmap to the ColorThief class.
`
  Bitmap imageBitmap = (Bitmap) extras.get("data");
  
  MMCQ.CMap result = ColorThief.getColorMap(imageBitmap, 5);
  
  MMCQ.VBox dominantColor = result.vboxes.get(0);
  
  int[] rgb = dominantColor.avg(false);
`
