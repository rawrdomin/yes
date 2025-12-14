import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SkeletonArcher {

    private final ImageView view;
    private final int maxHealth = 30;
    private int currentHealth = 30;
    private boolean alive = true;
    private double velocityY = 0;
    private double y;
    private Image idleImage;
    private final Image[] attackImages;
    private int currentAttackFrame = 0;
    private long lastAttackFrameTime = 0;
    private long lastAttackStartTime = 0;
    private final long attackFrameDuration = 180_000_000;
    private final long attackCooldown = 2_000_000_000L; 
    private boolean attacking = false;
    private boolean attackDamageDealt = false;
    private boolean arrowShot = false;

    public SkeletonArcher(double startX, double groundLevel) {
        idleImage = new Image(getClass().getResource("/images/ArcherIdle1.png").toExternalForm());
        view = new ImageView(idleImage);
        view.setFitWidth(200);
        view.setFitHeight(200);
        view.setX(startX);
        y = groundLevel;
        view.setY(y);
        attackImages = new Image[15];
        for (int i = 0; i < attackImages.length; i++) {
            attackImages[i] = new Image(getClass().getResource("/images/ArcherAttack" + (i + 1) + ".png").toExternalForm());
        }
    }

    public ImageView getView() {
        return view;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public boolean canDealDamage() {
        return attacking && !attackDamageDealt && currentAttackFrame == 10;
    }
    
    public boolean shouldShootArrow() {
        return attacking && currentAttackFrame == 10 && !arrowShot;
    }
    
    public void markArrowShot() {
        arrowShot = true;
    }
    
    public int getDirection() {
        return view.getScaleX() > 0 ? 1 : -1;
    }
    
    public double getCenterX() {
        return view.getX() + view.getFitWidth() / 2.0;
    }
    
    public double getCenterY() {
        return y + view.getFitHeight() / 2.0;
    }

    public void markAttackHit() {
        attackDamageDealt = true;
    }

    public void hit() {
        if (!alive) return;
        currentHealth -= 15;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public double getHealthPercentage() {
        return (double) currentHealth / maxHealth;
    }

    public void applyGravity(double gravity, double groundLevel) {
        if (!alive) return;
        velocityY += gravity;
        y += velocityY;
        double floor = groundLevel;
        if (y >= floor) {
            y = floor;
            velocityY = 0;
        }
        view.setY(y);
    }

    public void chaseAndAttack(double targetX, double minDistance, long now) {
        if (!alive) return;

        double centerX = view.getX() + view.getFitWidth() / 2.0;
        double distance = centerX - targetX;

        view.setScaleX(distance < 0 ? 1 : -1);

        if (attacking) {
            long elapsedAttack = now - lastAttackFrameTime;
            if (elapsedAttack >= attackFrameDuration) {
                currentAttackFrame++;
                lastAttackFrameTime = now;
                if (currentAttackFrame >= attackImages.length) {
                    attacking = false;
                    attackDamageDealt = false;
                    arrowShot = false;
                    currentAttackFrame = 0;
                    view.setImage(idleImage);
                } else {
                    view.setImage(attackImages[currentAttackFrame]);
                }
            }
            return;
        }

        if ((now - lastAttackStartTime) >= attackCooldown) {
            attacking = true;
            attackDamageDealt = false;
            arrowShot = false;
            currentAttackFrame = 0;
            lastAttackFrameTime = now;
            lastAttackStartTime = now;
            view.setImage(attackImages[0]);
        } else {
            view.setImage(idleImage);
        }
    }
}

