import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PlayerKnight extends Application {

    private final double characterSpeed = 10;
    private final double gravity = 1;
    private final double jumpStrength = -22;
    private final double groundLevel = 750;
    private final double dashDistance = 225;
    private final double dashSpeed = 30;
    private final long dashCooldown = 500_000_000;
    private final int attackFrameCount = 5;
    private final int runAttackFrameCount = 6;
    private final int runFrameCount = 7;
    private final int jumpFrameCount = 6;
    private final int hurtFrameCount = 2;
    private final long attackFrameDuration = 67_000_000;
    private final long attackCooldown = 200_000_000;
    private final long runAttackCooldown = 5_000_000;
    private final long runFrameDuration = 80_000_000; 
    private final long jumpFrameDuration = 100_000_000;
    private final long hurtFrameDuration = 100_000_000;
    private double velocityY = 0;
    private boolean isOnGround = true;
    private boolean hasDoubleJump = true;
    private boolean jumpKeyPressed = false; 
    private boolean isAttacking = false;
    private boolean attackHitApplied = false;
    private boolean isBlocking = false;
    private boolean isRunning = false;
    private boolean isDashing = false;
    private boolean isHurt = false;
    private double dashProgress = 0;
    private int dashDirection = 1; 
    private int dashInvisibleFrames = 0;
    private int dashAfterimageCounter = 0;
    private int currentAttackFrame = 0;
    private int currentRunFrame = 0;
    private int currentJumpFrame = 0;
    private int currentHurtFrame = 0;
    private long lastAttackFrameTime = 0;
    private long lastAttackStartTime = 0;
    private long lastRunAttackStartTime = 0;
    private long lastRunFrameTime = 0;
    private long lastJumpFrameTime = 0;
    private long lastDashTime = 0;
    private long lastHurtFrameTime = 0;
    private final Set<KeyCode> keysPressed = new HashSet<>();
    
    private final int maxHealth = 100;
    private int currentHealth = 100;

    @Override
    public void start(Stage stage) {
        final double screenWidth = 1920;
        final double screenHeight = 1080;
        
        Image backgroundImage = new Image(getClass().getResource("/images/Background2.png").toExternalForm());
        ImageView backgroundView = new ImageView(backgroundImage);
        
        backgroundView.setFitWidth(screenWidth);
        backgroundView.setPreserveRatio(true);

        Image[] attackImages = new Image[attackFrameCount];
        for (int i = 0; i < attackFrameCount; i++) {
            attackImages[i] = new Image(getClass().getResource("/images/Attack" + i + ".png").toExternalForm());
        }
        
        Image[] runAttackImages = new Image[runAttackFrameCount];
        for (int i = 0; i < runAttackFrameCount; i++) {
            runAttackImages[i] = new Image(getClass().getResource("/images/RunA" + (i + 1) + ".png").toExternalForm());
        }
        
        Image[] runImages = new Image[runFrameCount];
        for (int i = 0; i < runFrameCount; i++) {
            runImages[i] = new Image(getClass().getResource("/images/Run" + (i + 1) + ".png").toExternalForm());
        }
        
        Image[] jumpImages = new Image[jumpFrameCount];
        for (int i = 0; i < jumpFrameCount; i++) {
            jumpImages[i] = new Image(getClass().getResource("/images/Jump" + (i + 1) + ".png").toExternalForm());
        }
        
        Image[] hurtImages = new Image[hurtFrameCount];
        for (int i = 0; i < hurtFrameCount; i++) {
            hurtImages[i] = new Image(getClass().getResource("/images/Hurt" + (i + 1) + ".png").toExternalForm());
        }
        
        Image characterImage = new Image(getClass().getResource("/images/Idle1.png").toExternalForm());
        Image blockImage = new Image(getClass().getResource("/images/Block1.png").toExternalForm());
        ImageView character = new ImageView(characterImage);
        character.setFitWidth(200);
        character.setFitHeight(200);
        character.setX(125);
        character.setY(510);
        
        boolean[] facingRight = {true};
        
        List<ImageView> afterimages = new ArrayList<>();
        
        List<Skeleton> skeletons = new ArrayList<>();
        List<SkeletonArcher> skeletonArchers = new ArrayList<>();
        int[] currentWave = {1};

        final double healthBarWidth = 400;
        final double healthBarHeight = 35;
        final double healthBarX = (screenWidth - healthBarWidth) / 2;
        final double healthBarY = 50;
        final double cornerRadius = 10;
        
        Rectangle healthBarBackground = new Rectangle(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        healthBarBackground.setFill(Color.rgb(40, 40, 40));
        healthBarBackground.setStroke(Color.rgb(20, 20, 20));
        healthBarBackground.setStrokeWidth(3);
        healthBarBackground.setArcWidth(cornerRadius);
        healthBarBackground.setArcHeight(cornerRadius);
        
        Rectangle healthBarForeground = new Rectangle(healthBarX + 3, healthBarY + 3, healthBarWidth - 6, healthBarHeight - 6);
        healthBarForeground.setFill(Color.GREEN);
        healthBarForeground.setArcWidth(cornerRadius - 2);
        healthBarForeground.setArcHeight(cornerRadius - 2);
        
        Text healthText = new Text();
        healthText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        healthText.setFill(Color.WHITE);
        healthText.setX(healthBarX + healthBarWidth / 2 - 30);
        healthText.setY(healthBarY + healthBarHeight + 25);
        
        healthText.setText("100%");
        
        Text waveText = new Text();
        waveText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        waveText.setFill(Color.WHITE);
        waveText.setX(200);
        waveText.setY(200);
        waveText.setText("Wave: " + currentWave[0]);

        Pane root = new Pane();
        root.getChildren().addAll(backgroundView, character, healthBarBackground, healthBarForeground, healthText, waveText);
        
        spawnWave(currentWave[0], skeletons, skeletonArchers, root, groundLevel, screenWidth);
        
        List<DamageIndicator> damageIndicators = new ArrayList<>();

        Scene scene = new Scene(root, screenWidth, screenHeight);

        scene.setOnKeyPressed(event -> {
            keysPressed.add(event.getCode());
            if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.W) {
                jumpKeyPressed = true;
            }
        });
        scene.setOnKeyReleased(event -> keysPressed.remove(event.getCode()));

        scene.setOnMouseMoved(event -> {
                double mouseX = event.getSceneX();
                double mouseY = event.getSceneY();
                System.out.println("Mouse X: " + mouseX + ", Y: " + mouseY);
        });
        
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (isBlocking || isHurt) return;
                long now = System.nanoTime();

                boolean isCurrentlyRunning = keysPressed.contains(KeyCode.A) || keysPressed.contains(KeyCode.D);
                
                long requiredCooldown = isCurrentlyRunning ? runAttackCooldown : attackCooldown;
                long lastAttackTime = isCurrentlyRunning ? lastRunAttackStartTime : lastAttackStartTime;
                
                if (now - lastAttackTime >= requiredCooldown) {
                    isAttacking = true;
                    currentAttackFrame = 0;
                    lastAttackFrameTime = now;
                    attackHitApplied = false;
                    isRunning = false;
                    currentRunFrame = 0;

                    boolean useRunningAttackStart = isCurrentlyRunning || !isOnGround;
                    if (useRunningAttackStart) {
                        character.setImage(runAttackImages[0]);
                    } else {
                        character.setImage(attackImages[0]);
                    }
                    if (isCurrentlyRunning) {
                        lastRunAttackStartTime = now;
                    } else {
                        lastAttackStartTime = now;
                    }
                }
            } else if (event.getButton() == MouseButton.SECONDARY) {
                isBlocking = true;
                isAttacking = false;
                character.setImage(blockImage);
            }
        });

        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                isBlocking = false;
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean allDead = true;
                for (Skeleton skeleton : skeletons) {
                    if (skeleton.isAlive()) {
                        allDead = false;
                        break;
                    }
                }
                for (SkeletonArcher archer : skeletonArchers) {
                    if (archer.isAlive()) {
                        allDead = false;
                        break;
                    }
                }
                if (allDead && (!skeletons.isEmpty() || !skeletonArchers.isEmpty())) {
                    currentWave[0]++;
                    waveText.setText("Wave: " + currentWave[0]);
                    spawnWave(currentWave[0], skeletons, skeletonArchers, root, groundLevel, screenWidth);
                }
                
                double knightCenterX = character.getX() + character.getFitWidth() / 2.0;

                for (Skeleton skeleton : skeletons) {
                    if (!skeleton.isAlive()) {
                        continue;
                    }
                    
                    skeleton.applyGravity(gravity, groundLevel);
                    skeleton.chaseAndAttack(knightCenterX, 25, now);

                    if (skeleton.isAttacking() && skeleton.canDealDamage()) {
                        if (character.getBoundsInParent().intersects(skeleton.getView().getBoundsInParent())) {
                            if (!isBlocking && !isHurt) {
                                int damageTaken = 10;
                                currentHealth -= damageTaken;
                                currentHealth = Math.max(0, currentHealth);
                                
                                double characterCenterXPos = character.getX() + character.getFitWidth() / 2.0;
                                double characterTopY = character.getY();
                                DamageIndicator damageIndicator = new DamageIndicator(characterCenterXPos, characterTopY, damageTaken);
                                damageIndicators.add(damageIndicator);
                                root.getChildren().add(damageIndicator.text);
                                damageIndicator.text.applyCss();
                                
                                isHurt = true;
                                currentHurtFrame = 0;
                                lastHurtFrameTime = now;
                                isAttacking = false; 
                                isDashing = false;
                                character.setOpacity(1.0);
                                character.setImage(hurtImages[0]);
                            }
                            skeleton.markAttackHit();
                        }
                    }
                }
                
                for (SkeletonArcher archer : skeletonArchers) {
                    if (!archer.isAlive()) {
                        continue;
                    }
                    
                    archer.applyGravity(gravity, groundLevel);
                    archer.chaseAndAttack(knightCenterX, 25, now);

                    if (archer.isAttacking() && archer.canDealDamage()) {
                        if (character.getBoundsInParent().intersects(archer.getView().getBoundsInParent())) {
                            if (!isBlocking && !isHurt) {
                                int damageTaken = 10;
                                currentHealth -= damageTaken;
                                currentHealth = Math.max(0, currentHealth);
                                
                                double characterCenterXPos = character.getX() + character.getFitWidth() / 2.0;
                                double characterTopY = character.getY();
                                DamageIndicator damageIndicator = new DamageIndicator(characterCenterXPos, characterTopY, damageTaken);
                                damageIndicators.add(damageIndicator);
                                root.getChildren().add(damageIndicator.text);
                                damageIndicator.text.applyCss();
                                
                                isHurt = true;
                                currentHurtFrame = 0;
                                lastHurtFrameTime = now;
                                isAttacking = false; 
                                isDashing = false;
                                character.setOpacity(1.0);
                                character.setImage(hurtImages[0]);
                            }
                            archer.markAttackHit();
                        }
                    }
                }


                boolean isCurrentlyRunning = keysPressed.contains(KeyCode.A) || keysPressed.contains(KeyCode.D);
                
                if (isBlocking) {

                    character.setImage(blockImage);
                }
                else if (isHurt) {
                    character.setImage(hurtImages[currentHurtFrame]);
                    
                    long elapsed = now - lastHurtFrameTime;
                    if (elapsed >= hurtFrameDuration) {
                        currentHurtFrame++;
                        lastHurtFrameTime = now;
                        
                        if (currentHurtFrame >= hurtFrameCount) {
                            isHurt = false;
                            currentHurtFrame = 0;
                            if (!isOnGround) {
                                character.setImage(jumpImages[currentJumpFrame]);
                            } else if (isRunning) {
                                character.setImage(runImages[currentRunFrame]);
                            } else {
                                character.setImage(characterImage);
                            }
                        }
                    }
                }
                else if (isAttacking) {

                    boolean useRunningAttack = isCurrentlyRunning || !isOnGround;
                    int maxAttackFrames = useRunningAttack ? runAttackFrameCount : attackFrameCount;
                    
                    long elapsed = now - lastAttackFrameTime;
                    if (elapsed >= attackFrameDuration) {
                        currentAttackFrame++;
                        lastAttackFrameTime = now;
                        
                        if (currentAttackFrame >= maxAttackFrames) {
                            isAttacking = false;
                            currentAttackFrame = 0;
                            if (!isOnGround) {
                                character.setImage(jumpImages[currentJumpFrame]);
                            } else if (isRunning) {
                                character.setImage(runImages[currentRunFrame]);
                            } else {
                                character.setImage(characterImage);
                            }
                        } else {
                           
                            if (useRunningAttack) {
                                character.setImage(runAttackImages[currentAttackFrame]);
                            } else {
                                character.setImage(attackImages[currentAttackFrame]);
                            }
                        }
                    }
                    

                    if (!attackHitApplied) {
                        for (Skeleton skeleton : skeletons) {
                            if (skeleton.isAlive() && character.getBoundsInParent().intersects(skeleton.getView().getBoundsInParent())) {
                                skeleton.hit();
                                attackHitApplied = true;
                                if (!skeleton.isAlive()) {
                                    root.getChildren().remove(skeleton.getView());
                                }
                                break;
                            }
                        }
                        if (!attackHitApplied) {
                            for (SkeletonArcher archer : skeletonArchers) {
                                if (archer.isAlive() && character.getBoundsInParent().intersects(archer.getView().getBoundsInParent())) {
                                    archer.hit();
                                    attackHitApplied = true;
                                    if (!archer.isAlive()) {
                                        root.getChildren().remove(archer.getView());
                                    }
                                    break;
                                }
                            }
                        }
                    }  
                }
                else if (!isOnGround) {
                    long elapsed = now - lastJumpFrameTime;
                    if (elapsed >= jumpFrameDuration) {                    
                        if (currentJumpFrame < jumpFrameCount - 1) {
                            currentJumpFrame++;
                        } else {
                            currentJumpFrame = jumpFrameCount - 1;
                        }
                        lastJumpFrameTime = now;
                        character.setImage(jumpImages[currentJumpFrame]);
                    }
                }

                else if (isRunning) {
                    long elapsed = now - lastRunFrameTime;
                    if (elapsed >= runFrameDuration) {
                        currentRunFrame = (currentRunFrame + 1) % runFrameCount;
                        lastRunFrameTime = now;
                        character.setImage(runImages[currentRunFrame]);
                    }
                }
                

                if (keysPressed.contains(KeyCode.SHIFT) && !isDashing && !isHurt && (now - lastDashTime >= dashCooldown)) {
                    isDashing = true;
                    dashProgress = 0;
                    dashDirection = facingRight[0] ? 1 : -1;
                    dashInvisibleFrames = 2;
                    lastDashTime = now;
                }
                

                if (isDashing) {

                    if (dashInvisibleFrames > 0) {
                        character.setOpacity(0.0);
                        dashInvisibleFrames--;
                    } else {
                        character.setOpacity(1.0);
                    }
                    

                    dashAfterimageCounter++;
                    if (dashAfterimageCounter >= 5) {
                        dashAfterimageCounter = 0;
                        ImageView afterimage = new ImageView(character.getImage());
                        afterimage.setFitWidth(character.getFitWidth());
                        afterimage.setFitHeight(character.getFitHeight());
                        afterimage.setX(character.getX());
                        afterimage.setY(character.getY());
                        afterimage.setScaleX(character.getScaleX());
                        afterimage.setOpacity(0.4);
                        afterimages.add(afterimage);
                        root.getChildren().add(afterimage);
                    }
                    
                    double dashMove = dashSpeed;
                    double newX = character.getX() + (dashMove * dashDirection);
                    

                    // Allow going off both sides (will be wrapped by wrap-around check)
                    character.setX(newX);
                    dashProgress += dashMove;
                    
 
                    if (dashProgress >= dashDistance) {
                        isDashing = false;
                        dashProgress = 0;
                        dashAfterimageCounter = 0;
                        character.setOpacity(1.0); 
                    }
                }
                

                List<ImageView> toRemove = new ArrayList<>();
                for (ImageView afterimage : afterimages) {
                    double currentOpacity = afterimage.getOpacity();
                    currentOpacity -= 0.1;
                    afterimage.setOpacity(currentOpacity);
                    
                    if (currentOpacity <= 0) {
                        toRemove.add(afterimage);
                        root.getChildren().remove(afterimage);
                    }
                }
                afterimages.removeAll(toRemove);
                

                if (!isDashing && !isBlocking && !isHurt) {
                    boolean wasRunning = isRunning;
                    isRunning = false;
                    
                    if (keysPressed.contains(KeyCode.A)) {
                        isRunning = true;
                        if (facingRight[0]) {
                            character.setX(character.getX() - 65);
                            character.setScaleX(-1);
                            facingRight[0] = false;
                        }
                        character.setX(character.getX() - characterSpeed);
                    }
                    if (keysPressed.contains(KeyCode.D)) {
                        isRunning = true;
                        if (!facingRight[0]) {
                            character.setX(character.getX() + 65);
                            character.setScaleX(1);
                            facingRight[0] = true;
                        }
                        character.setX(character.getX() + characterSpeed);
                    }
                    

                    if (!isAttacking && !isHurt) {
                        if (wasRunning && !isRunning) {
                            character.setImage(characterImage);
                            currentRunFrame = 0;
                        }

                        else if (!wasRunning && isRunning) {
                            currentRunFrame = 0;
                            lastRunFrameTime = now;
                            character.setImage(runImages[0]);
                        }
                    }
                }
                

                if (jumpKeyPressed && !isBlocking && !isHurt) {
                    if (isOnGround) {

                        velocityY = jumpStrength;
                        isOnGround = false;
                        hasDoubleJump = true;
                        currentJumpFrame = 0;
                        lastJumpFrameTime = System.nanoTime();
                        character.setImage(jumpImages[0]);
                        jumpKeyPressed = false;
                    } else if (hasDoubleJump) {

                        velocityY = jumpStrength;
                        hasDoubleJump = false; 
                        currentJumpFrame = 0;
                        lastJumpFrameTime = System.nanoTime();
                        character.setImage(jumpImages[0]);
                        jumpKeyPressed = false;
                    } else {

                        jumpKeyPressed = false;
                    }
                }
            
                if (!isOnGround) {
                    velocityY += gravity;
                }

                double newY = character.getY() + velocityY;
                
                if (newY >= groundLevel) {
                    newY = groundLevel;
                    velocityY = 0;
                    isOnGround = true;
                    hasDoubleJump = true;
                    if (!isAttacking && !isBlocking && !isHurt) {
                        if (isRunning) {
                            character.setImage(runImages[currentRunFrame]);
                        } else {
                            character.setImage(characterImage);
                        }
                    }
                } else {
                    isOnGround = false;
                }
                
                character.setY(newY);
                
                double characterWidth = character.getFitWidth();
                double currentX = character.getX();
                if (currentX + characterWidth < 0) {
                    character.setX(screenWidth - characterWidth);
                } else if (currentX > screenWidth) {
                    character.setX(0);
                }
                
                List<DamageIndicator> indicatorsToRemove = new ArrayList<>();
                for (DamageIndicator indicator : damageIndicators) {
                    if (!indicator.update(now)) {
                        root.getChildren().remove(indicator.text);
                        indicatorsToRemove.add(indicator);
                    }
                }
                damageIndicators.removeAll(indicatorsToRemove);

                double healthPercentage = Math.max(0, (double) currentHealth / maxHealth);
                double currentHealthBarWidth = (healthBarWidth - 6) * healthPercentage;
                healthBarForeground.setWidth(currentHealthBarWidth);

                int healthPercent = (int) Math.round(healthPercentage * 100);
                healthText.setText(healthPercent + "%");
                double textWidth = healthText.getLayoutBounds().getWidth();
                healthText.setX(healthBarX + healthBarWidth / 2 - textWidth / 2);
            }
        };
        timer.start();

        stage.setScene(scene);
        stage.setTitle("Fallen Knight");
        stage.show();

        root.requestFocus();
    }

    private static class WaveConfig {
        int skeletons;
        int archers;
        
        WaveConfig(int skeletons, int archers) {
            this.skeletons = skeletons;
            this.archers = archers;
        }
    }
    
    // Wave configurations - (Warrior, Archer))
    private static WaveConfig[] waveConfigs = {
        new WaveConfig(1, 1), 
        new WaveConfig(2, 0),  
        new WaveConfig(2, 1),  
        new WaveConfig(3, 1), 
        new WaveConfig(3, 2),
    };
    
    private static void spawnWave(int waveNumber, List<Skeleton> skeletons, List<SkeletonArcher> skeletonArchers, Pane root, double groundLevel, double screenWidth) {
        for (Skeleton skeleton : skeletons) {
            root.getChildren().remove(skeleton.getView());
        }
        skeletons.clear();
        
        for (SkeletonArcher archer : skeletonArchers) {
            root.getChildren().remove(archer.getView());
        }
        skeletonArchers.clear();
        
        WaveConfig config;
        if (waveNumber <= waveConfigs.length) {
            config = waveConfigs[waveNumber - 1];
        } else {
            config = new WaveConfig(waveNumber, 0);
        }
        
        double skeletonWidth = 200;
        double spacing = 250;
        double startX = 900;
        int enemyIndex = 0;

        for (int i = 0; i < config.skeletons; i++) {
            double xPos = startX + (enemyIndex * spacing);
            while (xPos > screenWidth - skeletonWidth) {
                xPos = xPos - (screenWidth - skeletonWidth) + 100;
            }
            if (xPos < 400) {
                xPos = 400;
            }
            Skeleton skeleton = new Skeleton(xPos, groundLevel);
            skeletons.add(skeleton);
            root.getChildren().add(skeleton.getView());
            enemyIndex++;
        }

        for (int i = 0; i < config.archers; i++) {
            double xPos = startX + (enemyIndex * spacing);
            while (xPos > screenWidth - skeletonWidth) {
                xPos = xPos - (screenWidth - skeletonWidth) + 100;
            }
            if (xPos < 400) {
                xPos = 400;
            }
            SkeletonArcher archer = new SkeletonArcher(xPos, groundLevel);
            skeletonArchers.add(archer);
            root.getChildren().add(archer.getView());
            enemyIndex++;
        }
    }

    private static class DamageIndicator {
        Text text;
        double x;
        double y;
        double opacity;
        long startTime;
        static final long LIFETIME = 1_000_000_000L; 
        static final double FLOAT_SPEED = 0.8; 
        
        DamageIndicator(double x, double y, int damage) {
            this.x = x;
            this.y = y + 150;
            this.opacity = 1.0;
            this.startTime = System.nanoTime();
            
            this.text = new Text("-" + damage);
            this.text.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            this.text.setFill(Color.RED);

            this.text.setX(x);
            this.text.setY(y);
        }
        
        boolean update(long now) {
            long elapsed = now - startTime;
            
            y -= FLOAT_SPEED;
            text.setY(y);
            
            double textWidth = text.getLayoutBounds().getWidth();
            text.setX(x - textWidth / 2);
            
            if (elapsed > LIFETIME / 2) {
                double fadeProgress = (elapsed - LIFETIME / 2.0) / (LIFETIME / 2.0);
                opacity = Math.max(0, 1.0 - fadeProgress);
                Color currentColor = Color.color(1.0, 0.0, 0.0, opacity);
                text.setFill(currentColor);
            }
            
            return elapsed < LIFETIME && opacity > 0;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}