import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Skeleton {

    private final ImageView view;
    private int health = 3;
    private boolean alive = true;
    private double velocityY = 0;
    private double y;
    private double speed = 3.0;
    private final Image[] runImages;
    private final Image[] attackImages;
    private int currentRunFrame = 0;
    private int currentAttackFrame = 0;
    private long lastRunFrameTime = 0;
    private long lastAttackFrameTime = 0;
    private long lastAttackStartTime = 0;
    private final long runFrameDuration = 80_000_000; 
    private final long attackFrameDuration = 180_000_000;
    private final long attackCooldown = 2_000_000_000L; 
    private boolean attacking = false;
    private boolean attackDamageDealt = false;

    public Skeleton(double startX, double groundLevel) {
        Image skeletonImage = new Image(getClass().getResource("/images/SkeleIdle1.png").toExternalForm());
        view = new ImageView(skeletonImage);
        view.setFitWidth(200);
        view.setFitHeight(200);
        view.setX(startX);
        y = groundLevel;
        view.setY(y);

        runImages = new Image[8];
        for (int i = 0; i < runImages.length; i++) {
            runImages[i] = new Image(getClass().getResource("/images/SkeleRun" + (i + 1) + ".png").toExternalForm());
        }

        attackImages = new Image[4];
        for (int i = 0; i < attackImages.length; i++) {
            attackImages[i] = new Image(getClass().getResource("/images/SkeleAttack" + (i + 1) + ".png").toExternalForm());
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
        return attacking && !attackDamageDealt && currentAttackFrame >= 2 && currentAttackFrame < attackImages.length;
    }

    public void markAttackHit() {
        attackDamageDealt = true;
    }

    public void hit() {
        if (!alive) return;
        health -= 1;
        if (health <= 0) {
            alive = false;
        }
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

        if (attacking) {
            long elapsedAttack = now - lastAttackFrameTime;
            if (elapsedAttack >= attackFrameDuration) {
                currentAttackFrame++;
                lastAttackFrameTime = now;
                if (currentAttackFrame >= attackImages.length) {
                    attacking = false;
                    attackDamageDealt = false;
                    currentAttackFrame = 0;
                } else {
                    view.setImage(attackImages[currentAttackFrame]);
                }
            }
            return;
        }

        double centerX = view.getX() + view.getFitWidth() / 2.0;
        double distance = centerX - targetX;

        if (Math.abs(distance) <= minDistance && (now - lastAttackStartTime) >= attackCooldown) {
            attacking = true;
            attackDamageDealt = false;
            currentAttackFrame = 0;
            lastAttackFrameTime = now;
            lastAttackStartTime = now;
            view.setImage(attackImages[0]);

            view.setScaleX(distance < 0 ? 1 : -1);
            return;
        }

        if (Math.abs(distance) <= minDistance) {
            return;
        }

        double dir = distance < 0 ? 1 : -1;
        view.setX(view.getX() + dir * speed);
        view.setScaleX(dir);

        long elapsedRun = now - lastRunFrameTime;
        if (elapsedRun >= runFrameDuration) {
            currentRunFrame = (currentRunFrame + 1) % runImages.length;
            lastRunFrameTime = now;
            view.setImage(runImages[currentRunFrame]);
        }
    }
}
