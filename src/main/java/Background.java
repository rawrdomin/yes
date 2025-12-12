// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.scene.layout.Pane;

// /**
//  * Background component: displays a tiled or single background image and
//  * provides a simple parallax offset method. This is a self-contained Pane
//  * that can be added behind game objects.
//  */
// public class Background extends Pane {
//     private final ImageView bgView;

//     /**
//      * Create a background using a single image stretched to the scene size.
//      * Use imageResourcePath like "/images/Background2.png".
//      */
//     public Background(String imageResourcePath, double sceneWidth, double sceneHeight) {
//         Image bg = new Image(getClass().getResource(imageResourcePath).toExternalForm());
//         bgView = new ImageView(bg);
//         bgView.setFitWidth(sceneWidth);
//         bgView.setFitHeight(sceneHeight);
//         bgView.setPreserveRatio(false);
//         getChildren().add(bgView);

//         setPrefSize(sceneWidth, sceneHeight);
//     }

//     /**
//      * Simple parallax: shift the background horizontally and vertically by a fraction.
//      * factorX/factorY in [0,1] where smaller values move less (farther background).
//      */
//     public void setParallaxOffset(double offsetX, double offsetY, double factorX, double factorY) {
//         bgView.setTranslateX(-offsetX * factorX);
//         bgView.setTranslateY(-offsetY * factorY);
//     }

//     /**
//      * If you want a repeating/tiled background, you can extend this class to
//      * create multiple ImageViews and tile them horizontally/vertically.
//      */
// }