import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Arrow {
    private final ImageView view;
    private double x;
    private double y;
    private final double speed;
    private final int direction; // 1 for right, -1 for left
    private boolean active = true;
    
    public Arrow(double startX, double startY, int direction, double speed) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.speed = speed;
        
        Image arrowImage = new Image(getClass().getResource("/images/Arrow.png").toExternalForm());
        view = new ImageView(arrowImage);
        view.setFitWidth(50);
        view.setFitHeight(50);
        view.setX(x);
        view.setY(y);
        
        // Add glow effect to make arrow more visible
        Glow glow = new Glow(0.8);
        view.setEffect(glow);
        
        // Flip arrow based on direction
        if (direction < 0) {
            view.setScaleX(-1);
        }
    }
    
    public ImageView getView() {
        return view;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void update(double screenWidth) {
        if (!active) return;
        
        x += speed * direction;
        view.setX(x);
        
        // Deactivate if arrow goes off screen
        if (x < -100 || x > screenWidth + 100) {
            active = false;
        }
    }
    
    public void deactivate() {
        active = false;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}

