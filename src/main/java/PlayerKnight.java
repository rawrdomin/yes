import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
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

    // ========== GAME PHYSICS CONSTANTS ==========
    private final double characterSpeed = 10;
    private final double gravity = 1;
    private final double jumpStrength = -22;
    private final double groundLevel = 750;
    private final double dashDistance = 225;
    private final double dashSpeed = 30;
    private final long dashCooldown = 500_000_000;
    
    // ========== ANIMATION FRAME COUNTS ==========
    private final int attackFrameCount = 5;
    private final int runAttackFrameCount = 6;
    private final int runFrameCount = 7;
    private final int jumpFrameCount = 6;
    private final int hurtFrameCount = 2;
    private final int deathFrameCount = 6;
    
    // ========== ANIMATION TIMING (in nanoseconds) ==========
    private final long attackFrameDuration = 67_000_000;
    private final long attackCooldown = 200_000_000;
    private final long runAttackCooldown = 5_000_000;
    private final long runFrameDuration = 80_000_000;
    private final long jumpFrameDuration = 100_000_000;
    private final long hurtFrameDuration = 100_000_000;
    private final long deathFrameDuration = 150_000_000;
    // ========== PLAYER STATE VARIABLES ==========
    private double velocityY = 0;                     // Current vertical velocity (positive = down)
    private boolean isOnGround = true;                // Whether player is touching the ground
    private boolean hasDoubleJump = true;             // Whether player can still use double jump
    private boolean jumpKeyPressed = false;           // Flag to handle jump input
    private boolean isAttacking = false;               // Whether player is currently attacking
    private boolean attackHitApplied = false;          // Prevents multiple hits from single attack
    private boolean isBlocking = false;                // Whether player is blocking (right mouse button)
    private boolean isRunning = false;                 // Whether player is running horizontally
    private boolean isDashing = false;                // Whether player is currently dashing
    private boolean isHurt = false;                    // Whether player is in hurt state (invincibility)
    private boolean isDead = false;                    // Whether player has died
    
    // ========== DASH MECHANICS ==========
    private double dashProgress = 0;                  // Distance traveled during current dash
    private int dashDirection = 1;                    // Direction of dash (1 = right, -1 = left)
    private int dashInvisibleFrames = 0;              // Frames remaining where player is invisible during dash
    private int dashAfterimageCounter = 0;            // Counter for creating dash afterimages
    
    // ========== ANIMATION FRAME TRACKING ==========
    private int currentAttackFrame = 0;
    private int currentRunFrame = 0;
    private int currentJumpFrame = 0;
    private int currentHurtFrame = 0;
    private int currentDeathFrame = 0;
    
    // ========== TIMING TRACKERS (nanoseconds) ==========
    private long lastAttackFrameTime = 0;
    private long lastAttackStartTime = 0;
    private long lastRunAttackStartTime = 0;
    private long lastRunFrameTime = 0;
    private long lastJumpFrameTime = 0;
    private long lastDashTime = 0;
    private long lastHurtFrameTime = 0;
    private long lastDeathFrameTime = 0;
    private long lastFootstepTime = 0;
    private final long footstepInterval = 200_000_000L;
    
    // ========== INPUT HANDLING ==========
    private final Set<KeyCode> keysPressed = new HashSet<>();
    
    // ========== HEALTH SYSTEM ==========
    private final int maxHealth = 100;                // Maximum health value
    private int currentHealth = 100;                  // Current health value
    
    // ========== WAVE SYSTEM ==========
    private boolean isWaveTransitioning = false;       // Whether currently transitioning between waves
    private long waveTransitionStartTime = 0;          // When wave transition started
    private final long waveTransitionDuration = 3_000_000_000L; // Duration of wave transition (3 seconds)
    private boolean healingApplied = false;            // Whether healing was applied during current transition
    
    // ========== GAME STATE ==========
    private AnimationTimer gameTimer;                  // Main game loop timer
    private boolean isPaused = false;                  // Whether game is paused
    private boolean gameOverScreenShown = false;      // Whether game over screen has been displayed
    private long deathAnimationCompleteTime = 0;      // When death animation finished
    private final long gameOverScreenDelay = 1_500_000_000L; // Delay before showing game over (1.5 seconds)
    private String knightName = "Knight";             // Player's chosen knight name
    
    // // Sound system
    // private MediaPlayer menuMusicPlayer;
    // private MediaPlayer fightMusicPlayer;
    // private MediaPlayer deathMusicPlayer;
    // private MediaPlayer currentMusicPlayer;
    // // Sound effects
    // private Media archerShootSound;
    // private Media dashSound;
    // private Media deathSound;
    // private Media footstepSound;
    // private Media guardSound;
    // private Media hurtSound;
    // private Media jumpSound;
    // private Media skeleDeathSound;
    // private Media[] skeleWalkSounds;
    // private Media sword1Sound;
    // private Media sword2Sound;
    // private boolean useSword1 = true; // Alternate between sword sounds
    // private int currentSkeleWalkIndex = 0;

    /**
     * JavaFX Application entry point
     * Initializes the game and shows the main menu
     */
    @Override
    public void start(Stage stage) {
        // initializeSounds();
        showMenu(stage);
    }
    
    // private void initializeSounds() {
    //     // Background music
    //     Media menuMusic = new Media(getClass().getResource("/Sounds/01 No Escape.mp3").toExternalForm());
    //     menuMusicPlayer = new MediaPlayer(menuMusic);
    //     menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        
    //     Media fightMusic = new Media(getClass().getResource("/Sounds/13. Conjonctivius.mp3").toExternalForm());
    //     fightMusicPlayer = new MediaPlayer(fightMusic);
    //     fightMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        
    //     Media deathMusic = new Media(getClass().getResource("/Sounds/18 Death and I.mp3").toExternalForm());
    //     deathMusicPlayer = new MediaPlayer(deathMusic);
    //     deathMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        
    //     // Sound effects
    //     archerShootSound = new Media(getClass().getResource("/Sounds/Archer.ogg").toExternalForm());
    //     dashSound = new Media(getClass().getResource("/Sounds/Dash.wav").toExternalForm());
    //     deathSound = new Media(getClass().getResource("/Sounds/Death.wav").toExternalForm());
    //     footstepSound = new Media(getClass().getResource("/Sounds/Footstep.wav").toExternalForm());
    //     guardSound = new Media(getClass().getResource("/Sounds/Guard.wav").toExternalForm());
    //     hurtSound = new Media(getClass().getResource("/Sounds/Hurt.wav").toExternalForm());
    //     jumpSound = new Media(getClass().getResource("/Sounds/Jump.wav").toExternalForm());
    //     skeleDeathSound = new Media(getClass().getResource("/Sounds/SkeleDeath.ogg").toExternalForm());
        
    //     // Skeleton walk sounds
    //     skeleWalkSounds = new Media[4];
    //     for (int i = 0; i < 4; i++) {
    //         skeleWalkSounds[i] = new Media(getClass().getResource("/Sounds/SkeleWalk" + (i + 1) + ".ogg").toExternalForm());
    //     }
        
    //     sword1Sound = new Media(getClass().getResource("/Sounds/Sword1.wav").toExternalForm());
    //     sword2Sound = new Media(getClass().getResource("/Sounds/Sword2.wav").toExternalForm());
    // }
    
    // private void playSound(Media sound) {
    //     MediaPlayer player = new MediaPlayer(sound);
    //     player.play();
    // }

    /**
     * Displays the main menu screen with clickable areas for navigation
     * Handles clicks on Play, Instructions, Leaderboard, and Exit buttons
     */
    private void showMenu(Stage stage) {
        final double screenWidth = 1720;
        final double screenHeight = 1080;

        // Load and display the main menu background image
        Image menuImage = new Image(getClass().getResource("/images/Start Screen.png").toExternalForm());
        ImageView menuView = new ImageView(menuImage);
        menuView.setFitWidth(screenWidth);
        menuView.setFitHeight(screenHeight);
        menuView.setPreserveRatio(false);
        menuView.setSmooth(true);
        menuView.setX(0);
        menuView.setY(0);

        // Create the main menu pane and bind image to pane size
        Pane menuPane = new Pane(menuView);
        menuView.fitWidthProperty().bind(menuPane.widthProperty());
        menuView.fitHeightProperty().bind(menuPane.heightProperty());

        // Create leaderboard image
        Image leaderboardImage = new Image(getClass().getResource("/images/LB.png").toExternalForm());
        ImageView leaderboardView = new ImageView(leaderboardImage);
        leaderboardView.setFitWidth(600);
        leaderboardView.setFitHeight(1200);
        leaderboardView.setPreserveRatio(true);
        leaderboardView.setSmooth(true);
        leaderboardView.setLayoutX(screenWidth - 670);
        leaderboardView.setLayoutY(95);
        leaderboardView.setVisible(false);
        menuPane.getChildren().add(leaderboardView);

        Scene menuScene = new Scene(menuPane, screenWidth, screenHeight);

        // Handle mouse clicks on menu buttons
        menuPane.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Play button area - shows name entry screen
            if (mouseX >= 70 && mouseX <= 150 && mouseY >= 520 && mouseY <= 560) {
                showNameEntry(stage);
            } 
            // Instructions button area - shows how to play screen
            else if (mouseX >= 70 && mouseX <= 300 && mouseY >= 600 && mouseY <= 640) {
                showInstructions(stage);
            } 
            // Leaderboard button area - toggles leaderboard visibility
            else if (mouseX >= 70 && mouseX <= 305 && mouseY >= 760 && mouseY <= 800) {
                leaderboardView.setVisible(!leaderboardView.isVisible());
            } 
            // Exit button area - closes the game
            else if (mouseX >= 70 && mouseX <= 145 && mouseY >= 845 && mouseY <= 880) {
                System.exit(0);
            } 
            // Clicking elsewhere hides the leaderboard
            else {
                leaderboardView.setVisible(false);
            }
        });

        // Debug: Output mouse position for button area calibration
        // menuScene.setOnMouseMoved(event -> {
        //     System.out.println("Mouse X: " + event.getSceneX() + ", Y: " + event.getSceneY());
        // });

        // Start menu music
        // if (currentMusicPlayer != null) {
        //     currentMusicPlayer.stop();
        // }
        // menuMusicPlayer.play();
        // currentMusicPlayer = menuMusicPlayer;
        
        stage.setScene(menuScene);
        stage.setTitle("Fallen Knight");
        stage.setResizable(false);
        stage.show();
    }



    /**
     * Displays the instructions/how to play screen
     */
    private void showInstructions(Stage stage) {
        final double screenWidth = 1720;
        final double screenHeight = 1080;

        // Load and display the instructions image
        Image instructionImage = new Image(getClass().getResource("/images/How to Play.png").toExternalForm());
        ImageView instructionView = new ImageView(instructionImage);
        instructionView.setFitWidth(screenWidth);
        instructionView.setFitHeight(screenHeight);
        instructionView.setPreserveRatio(true);
        instructionView.setSmooth(false);

        // Add text instruction at bottom of screen
        Text instructionsText = new Text("Press ESC to return to menu");
        instructionsText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        instructionsText.setFill(Color.WHITE);
        instructionsText.setX(70);
        instructionsText.setY(925);

        // Create pane with instruction image and text
        Pane instructionPane = new Pane(instructionView, instructionsText);

        // Create scene with black background
        Scene instructionScene = new Scene(instructionPane, screenWidth, screenHeight);
        instructionScene.setFill(Color.BLACK);

        // Handle ESC key to return to main menu
        instructionScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                showMenu(stage);
            }
        });

        stage.setScene(instructionScene);
    }

    /**
     * Displays the name entry screen where player enters their knight's name
     */
    private void showNameEntry(Stage stage) {
        final double screenWidth = 1720;
        final double screenHeight = 1080;
        
        // Create overlay pane with dark semi-transparent background
        Pane nameEntryPane = new Pane();
        nameEntryPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        
        // Title text prompting for name entry
        Text titleText = new Text("Enter Your Knight's Name");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleText.setFill(Color.WHITE);
        titleText.applyCss();
        double titleWidth = titleText.getLayoutBounds().getWidth();
        titleText.setX((screenWidth - titleWidth) / 2);
        titleText.setY(screenHeight / 2 - 100);
        
        // Text field for player to enter their name
        TextField nameField = new TextField();
        nameField.setPromptText("Enter name...");
        nameField.setFont(Font.font("Arial", FontWeight.NORMAL, 32));
        nameField.setPrefWidth(500);
        nameField.setPrefHeight(60);
        nameField.setLayoutX((screenWidth - 500) / 2);
        nameField.setLayoutY(screenHeight / 2 - 30);
        nameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-text-fill: black;");
        
        // Instruction text telling player to press ENTER
        Text instructionText = new Text("Press ENTER to start");
        instructionText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        instructionText.setFill(Color.WHITE);
        instructionText.applyCss();
        double instructionWidth = instructionText.getLayoutBounds().getWidth();
        instructionText.setX((screenWidth - instructionWidth) / 2);
        instructionText.setY(screenHeight / 2 + 80);
        
        // Cancel text telling player they can press ESC
        Text cancelText = new Text("Press ESC to cancel");
        cancelText.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        cancelText.setFill(Color.GRAY);
        cancelText.applyCss();
        double cancelWidth = cancelText.getLayoutBounds().getWidth();
        cancelText.setX((screenWidth - cancelWidth) / 2);
        cancelText.setY(screenHeight / 2 + 130);
        
        nameEntryPane.getChildren().addAll(titleText, nameField, instructionText, cancelText);
        
        Scene nameEntryScene = new Scene(nameEntryPane, screenWidth, screenHeight);
        
        // Handle keyboard input
        nameEntryScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String enteredName = nameField.getText().trim();
                if (!enteredName.isEmpty()) {
                    knightName = enteredName;
                } else {
                    knightName = "Knight"; // Default name if field is empty
                }
                startGame(stage);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // Return to main menu without starting game
                showMenu(stage);
            }
        });
        
        stage.setScene(nameEntryScene);
        // Automatically focus the text field so player can type immediately
        nameField.requestFocus();
    }

    /**
     * Displays the game over screen showing the knight's name and waves survived
     */
    private void showGameOverScreen(Stage stage, int wavesSurvived) {
        final double screenWidth = 1920;
        final double screenHeight = 1080;
        
        Pane overlayPane = new Pane();
        overlayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        
        // "Game Over" title text
        Text resultsTitle = new Text("Game Over");
        resultsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        resultsTitle.setFill(Color.WHITE);
        resultsTitle.applyCss();
        double titleWidth = resultsTitle.getLayoutBounds().getWidth();
        resultsTitle.setX((screenWidth - titleWidth) / 2);
        resultsTitle.setY(screenHeight / 2 - 200);
        
        // Display the knight's name that was entered at game start
        Text knightNameText = new Text(knightName);
        knightNameText.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        knightNameText.setFill(Color.WHITE);
        knightNameText.applyCss();
        double nameTextWidth = knightNameText.getLayoutBounds().getWidth();
        knightNameText.setX((screenWidth - nameTextWidth) / 2);
        knightNameText.setY(screenHeight / 2 - 100);
        
        // Display how many waves the player survived
        Text wavesText = new Text("Waves Survived: " + wavesSurvived);
        wavesText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        wavesText.setFill(Color.WHITE);
        wavesText.applyCss();
        double wavesTextWidth = wavesText.getLayoutBounds().getWidth();
        wavesText.setX((screenWidth - wavesTextWidth) / 2);
        wavesText.setY(screenHeight / 2);
        
        // Instruction text for returning to menu
        Text clickText = new Text("Click to return to main menu");
        clickText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        clickText.setFill(Color.WHITE);
        clickText.applyCss();
        double clickTextWidth = clickText.getLayoutBounds().getWidth();
        clickText.setX((screenWidth - clickTextWidth) / 2);
        clickText.setY(screenHeight / 2 + 100);
        
        overlayPane.getChildren().addAll(resultsTitle, knightNameText, wavesText, clickText);
        
        // Handle mouse click to return to main menu
        overlayPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Reset all game state variables
                isDead = false;
                gameOverScreenShown = false;
                deathAnimationCompleteTime = 0;
                currentHealth = maxHealth;
                showMenu(stage);
            }
        });
        
        Scene gameOverScene = new Scene(overlayPane, screenWidth, screenHeight);
        stage.setScene(gameOverScene);
    }

    /**
     * Displays the pause menu overlay when player presses ESC during gameplay
     */
    private void showPauseMenu(Stage stage, Scene gameScene, Pane gameRoot) {
        final double screenWidth = 1920;
        final double screenHeight = 1080;
        
        // Pause the game timer and set paused flag
        isPaused = true;
        gameTimer.stop();
        
        // Create overlay pane with semi-transparent background
        Pane overlayPane = new Pane();
        overlayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
        
        // Load and display the pause menu image
        Image pauseImage = new Image(getClass().getResource("/images/Paused.png").toExternalForm());
        ImageView pauseView = new ImageView(pauseImage);
        
        // Set pause menu size and center it on screen
        double pauseMenuWidth = 500;
        double pauseMenuHeight = 400;
        pauseView.setFitWidth(pauseMenuWidth);
        pauseView.setFitHeight(pauseMenuHeight);
        pauseView.setPreserveRatio(true);
        pauseView.setSmooth(true);
        
        // Center the pause menu on screen
        double pauseMenuX = (screenWidth - pauseMenuWidth) / 2;
        double pauseMenuY = (screenHeight - pauseMenuHeight) / 2;
        pauseView.setX(pauseMenuX);
        pauseView.setY(pauseMenuY);
        
        overlayPane.getChildren().add(pauseView);
        
        // Define button hitbox areas for click detection
        double buttonStartX = pauseMenuX;
        double buttonWidth = pauseMenuWidth;
        double buttonHeight = 50;
        double buttonSpacing = 10;
        
        // Resume button area coordinates
        double resumeButtonY = pauseMenuY + 100;
        double resumeButtonX = buttonStartX;
        
        // Return to main menu button area coordinates
        double menuButtonY = resumeButtonY + buttonHeight + buttonSpacing + 25;
        double menuButtonX = buttonStartX;
        
        // Handle clicks on pause menu buttons
        overlayPane.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            
            // Resume button clicked
            if (mouseX >= resumeButtonX && mouseX <= resumeButtonX + buttonWidth && 
                mouseY >= resumeButtonY && mouseY <= resumeButtonY + buttonHeight) {
                isPaused = false;
                gameTimer.start();
                stage.setScene(gameScene);
            }
            // Return to main menu button clicked
            else if (mouseX >= menuButtonX && mouseX <= menuButtonX + buttonWidth && 
                     mouseY >= menuButtonY && mouseY <= menuButtonY + buttonHeight) {
                isPaused = false;
                gameTimer.stop();
                // Stop fight music and return to menu music
                // if (currentMusicPlayer != null) {
                //     currentMusicPlayer.stop();
                // }
                showMenu(stage);
            }
        });
        
        Scene pauseScene = new Scene(overlayPane, screenWidth, screenHeight);
        
        // Handle ESC key to resume game
        pauseScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                isPaused = false;
                gameTimer.start();
                stage.setScene(gameScene);
            }
        });
        
        stage.setScene(pauseScene);
    }

    /**
     * Initializes and starts the main game
     * Sets up all game objects, UI elements, input handlers, and the game loop
     */
    private void startGame(Stage stage) {
        // ========== RESET ALL GAME STATE ==========
        isDead = false;
        gameOverScreenShown = false;
        deathAnimationCompleteTime = 0;
        currentHealth = maxHealth;
        isPaused = false;
        isAttacking = false;
        isBlocking = false;
        isHurt = false;
        isRunning = false;
        isDashing = false;
        currentDeathFrame = 0;
        velocityY = 0;
        isOnGround = true;
        hasDoubleJump = true;
        
        // Switch to fight music (commented out)
        // if (currentMusicPlayer != null) {
        //     currentMusicPlayer.stop();
        // }
        // fightMusicPlayer.play();
        // currentMusicPlayer = fightMusicPlayer;
        
        final double screenWidth = 1920;
        final double screenHeight = 1080;
        
        // ========== LOAD BACKGROUND ==========
        Image backgroundImage = new Image(getClass().getResource("/images/Background2.png").toExternalForm());
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(screenWidth);
        backgroundView.setPreserveRatio(true);

        // ========== LOAD ALL ANIMATION SPRITES ==========
        // Load attack animation frames
        Image[] attackImages = new Image[attackFrameCount];
        for (int i = 0; i < attackFrameCount; i++) {
            attackImages[i] = new Image(getClass().getResource("/images/Attack" + i + ".png").toExternalForm());
        }
        
        // Load running attack animation frames
        Image[] runAttackImages = new Image[runAttackFrameCount];
        for (int i = 0; i < runAttackFrameCount; i++) {
            runAttackImages[i] = new Image(getClass().getResource("/images/RunA" + (i + 1) + ".png").toExternalForm());
        }
        
        // Load running animation frames
        Image[] runImages = new Image[runFrameCount];
        for (int i = 0; i < runFrameCount; i++) {
            runImages[i] = new Image(getClass().getResource("/images/Run" + (i + 1) + ".png").toExternalForm());
        }
        
        // Load jump animation frames
        Image[] jumpImages = new Image[jumpFrameCount];
        for (int i = 0; i < jumpFrameCount; i++) {
            jumpImages[i] = new Image(getClass().getResource("/images/Jump" + (i + 1) + ".png").toExternalForm());
        }
        
        // Load hurt animation frames
        Image[] hurtImages = new Image[hurtFrameCount];
        for (int i = 0; i < hurtFrameCount; i++) {
            hurtImages[i] = new Image(getClass().getResource("/images/Hurt" + (i + 1) + ".png").toExternalForm());
        }
        
        // Load death animation frames
        Image[] deathImages = new Image[deathFrameCount];
        for (int i = 0; i < deathFrameCount; i++) {
            deathImages[i] = new Image(getClass().getResource("/images/Dead" + (i + 1) + ".png").toExternalForm());
        }
        
        // ========== CREATE PLAYER CHARACTER ==========
        Image characterImage = new Image(getClass().getResource("/images/Idle1.png").toExternalForm());
        Image blockImage = new Image(getClass().getResource("/images/Block1.png").toExternalForm());
        ImageView character = new ImageView(characterImage);
        character.setFitWidth(200);
        character.setFitHeight(200);
        character.setX(125);
        character.setY(510);
        
        // Track which direction character is facing (true = right, false = left)
        boolean[] facingRight = {true};
        
        // ========== GAME OBJECT LISTS ==========
        List<ImageView> afterimages = new ArrayList<>(); // Dash afterimage effects
        List<Skeleton> skeletons = new ArrayList<>(); // Melee skeleton enemies
        List<SkeletonArcher> skeletonArchers = new ArrayList<>(); // Ranged skeleton enemies
        List<Arrow> arrows = new ArrayList<>(); // Arrows shot by archers
        Map<Skeleton, Rectangle[]> skeletonHealthBars = new HashMap<>(); // HP bars for skeletons
        Map<SkeletonArcher, Rectangle[]> archerHealthBars = new HashMap<>(); // HP bars for archers
        int[] currentWave = {1}; // Current wave number (using array for pass-by-reference)

        // ========== UI ELEMENTS - HEALTH BAR ==========
        final double healthBarWidth = 400;
        final double healthBarHeight = 35;
        final double healthBarX = (screenWidth - healthBarWidth) / 2; // Center horizontally
        final double healthBarY = 50;
        final double cornerRadius = 10;
        
        // Enemy HP bar dimensions (smaller bars above enemies)
        final double enemyHealthBarWidth = 120;
        final double enemyHealthBarHeight = 8;
        final double enemyHealthBarCornerRadius = 4;
        
        // Create health bar background (dark gray border)
        Rectangle healthBarBackground = new Rectangle(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        healthBarBackground.setFill(Color.rgb(40, 40, 40));
        healthBarBackground.setStroke(Color.rgb(20, 20, 20));
        healthBarBackground.setStrokeWidth(3);
        healthBarBackground.setArcWidth(cornerRadius);
        healthBarBackground.setArcHeight(cornerRadius);
        
        // Create health bar foreground (green fill) - initialized to full health
        double healthPercentage = Math.max(0, (double) currentHealth / maxHealth);
        double initialHealthBarWidth = (healthBarWidth - 6) * healthPercentage;
        Rectangle healthBarForeground = new Rectangle(healthBarX + 3, healthBarY + 3, initialHealthBarWidth, healthBarHeight - 6);
        healthBarForeground.setFill(Color.GREEN);
        healthBarForeground.setArcWidth(cornerRadius - 2);
        healthBarForeground.setArcHeight(cornerRadius - 2);
        
        // Health percentage text display
        Text healthText = new Text();
        healthText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        healthText.setFill(Color.WHITE);
        healthText.setX(healthBarX + healthBarWidth / 2 - 30);
        healthText.setY(healthBarY + healthBarHeight + 25);
        
        // Set initial health text to 100%
        int healthPercent = (int) Math.round(healthPercentage * 100);
        healthText.setText(healthPercent + "%");
        double textWidth = healthText.getLayoutBounds().getWidth();
        healthText.setX(healthBarX + healthBarWidth / 2 - textWidth / 2);
        
        // ========== UI ELEMENTS - WAVE TEXT ==========
        Text waveText = new Text();
        waveText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        waveText.setFill(Color.WHITE);
        waveText.setX(200);
        waveText.setY(200);
        waveText.setText("Wave: " + currentWave[0]);
        
        // ========== UI ELEMENTS - DEATH TEXT ==========
        Text deathText = new Text();
        deathText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        deathText.setFill(Color.WHITE);
        deathText.setText("You Have Fallen...");
        deathText.setVisible(false);
        deathText.setY(screenHeight / 2);
        
        // ========== UI ELEMENTS - HEALING INDICATOR ==========
        Text healingText = new Text();
        healingText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        healingText.setFill(Color.GREEN);
        healingText.setText("+15");
        healingText.setVisible(false);
        healingText.setX(healthBarX + healthBarWidth + 20);
        healingText.setY(healthBarY + healthBarHeight / 2 + 8);

        // ========== CREATE ROOT PANE AND ADD ALL ELEMENTS ==========
        Pane root = new Pane();
        root.getChildren().addAll(backgroundView, character, healthBarBackground, healthBarForeground, healthText, waveText, deathText, healingText);
        
        // Spawn the first wave of enemies
        spawnWave(currentWave[0], skeletons, skeletonArchers, arrows, skeletonHealthBars, archerHealthBars, root, groundLevel, screenWidth, enemyHealthBarWidth, enemyHealthBarHeight, enemyHealthBarCornerRadius);
        
        // List to track floating damage numbers
        List<DamageIndicator> damageIndicators = new ArrayList<>();

        // ========== CREATE GAME SCENE ==========
        Scene scene = new Scene(root, screenWidth, screenHeight);

        // ========== KEYBOARD INPUT HANDLING ==========
        scene.setOnKeyPressed(event -> {
            // ESC key opens pause menu
            if (event.getCode() == KeyCode.ESCAPE) {
                if (!isPaused && !isDead) {
                    showPauseMenu(stage, scene, root);
                }
                return;
            }
            // Disable all other input when dead or paused
            if (isDead || isPaused) return;
            
            // Track which keys are currently pressed
            keysPressed.add(event.getCode());
            
            // Handle jump input (SPACE or W key)
            if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.W) {
                jumpKeyPressed = true;
            }
        });
        
        // Remove keys from pressed set when released
        scene.setOnKeyReleased(event -> keysPressed.remove(event.getCode()));

        // Debug: Output mouse position for calibration
        scene.setOnMouseMoved(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            System.out.println("Mouse X: " + mouseX + ", Y: " + mouseY);
        });
        
        // ========== MOUSE INPUT HANDLING ==========
        scene.setOnMousePressed(event -> {
            // Left mouse button - Attack
            if (event.getButton() == MouseButton.PRIMARY) {
                // Can't attack while blocking, hurt, or dead
                if (isBlocking || isHurt || isDead) return;
                
                long now = System.nanoTime();
                
                // Check if player is currently moving (determines attack type)
                boolean isCurrentlyRunning = keysPressed.contains(KeyCode.A) || keysPressed.contains(KeyCode.D);
                
                // Use different cooldown for running vs standing attacks
                long requiredCooldown = isCurrentlyRunning ? runAttackCooldown : attackCooldown;
                long lastAttackTime = isCurrentlyRunning ? lastRunAttackStartTime : lastAttackStartTime;
                
                // Check if attack cooldown has passed
                if (now - lastAttackTime >= requiredCooldown) {
                    // Start attack animation
                    isAttacking = true;
                    currentAttackFrame = 0;
                    lastAttackFrameTime = now;
                    attackHitApplied = false;
                    isRunning = false;
                    currentRunFrame = 0;

                    // Play sword sound (commented out)
                    // if (useSword1) {
                    //     playSound(sword1Sound);
                    // } else {
                    //     playSound(sword2Sound);
                    // }
                    // useSword1 = !useSword1;

                    // Choose attack animation based on movement state
                    boolean useRunningAttackStart = isCurrentlyRunning || !isOnGround;
                    if (useRunningAttackStart) {
                        character.setImage(runAttackImages[0]);
                    } else {
                        character.setImage(attackImages[0]);
                    }
                    
                    // Track attack start time based on type
                    if (isCurrentlyRunning) {
                        lastRunAttackStartTime = now;
                    } else {
                        lastAttackStartTime = now;
                    }
                }
            } 
            // Right mouse button - Block
            else if (event.getButton() == MouseButton.SECONDARY) {
                isBlocking = true;
                isAttacking = false;
                character.setImage(blockImage);
            }
        });

        // Release block when right mouse button is released
        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                isBlocking = false;
            }
        });

        // ========== MAIN GAME LOOP ==========
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Skip all updates if game is paused
                if (isPaused) {
                    return;
                }
                
                // ========== DEATH HANDLING ==========
                // If player is dead, only play death animation and show game over screen
                if (isDead) {
                    // Update death animation frames
                    long elapsed = now - lastDeathFrameTime;
                    if (elapsed >= deathFrameDuration) {
                        currentDeathFrame++;
                        lastDeathFrameTime = now;
                        
                        // Play through all death frames
                        if (currentDeathFrame < deathFrameCount) {
                            character.setImage(deathImages[currentDeathFrame]);
                        } else {
                            // Keep showing last frame after animation completes
                            character.setImage(deathImages[deathFrameCount - 1]);
                            // Mark when death animation finishes
                            if (deathAnimationCompleteTime == 0) {
                                deathAnimationCompleteTime = now;
                            }
                        }
                    }
                    
                    // Show game over screen after a delay once animation completes
                    if (deathAnimationCompleteTime > 0 && !gameOverScreenShown) {
                        long timeSinceAnimationComplete = now - deathAnimationCompleteTime;
                        if (timeSinceAnimationComplete >= gameOverScreenDelay) {
                            gameOverScreenShown = true;
                            gameTimer.stop();
                            // Calculate waves survived (current wave - 1, since they died in current wave)
                            int wavesSurvived = Math.max(0, currentWave[0] - 1);
                            showGameOverScreen(stage, wavesSurvived);
                        }
                    }
                    
                    return;
                }
                
                // ========== WAVE TRANSITION SYSTEM ==========
                // Check if all enemies in current wave are dead
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
                
                // Start wave transition if all enemies are dead and wave wasn't empty
                if (allDead && (!skeletons.isEmpty() || !skeletonArchers.isEmpty()) && !isWaveTransitioning) {
                    // Start wave transition timer
                    isWaveTransitioning = true;
                    waveTransitionStartTime = now;
                    healingApplied = false;
                    
                    // Remove all arrows when wave ends
                    for (Arrow arrow : arrows) {
                        root.getChildren().remove(arrow.getView());
                    }
                    arrows.clear();
                }
                
                // Handle wave transition period (3 seconds between waves)
                if (isWaveTransitioning) {
                    long elapsed = now - waveTransitionStartTime;
                    
                    // Apply healing once at the start of transition
                    if (!healingApplied) {
                        currentHealth += 15; // Heal 15 HP between waves
                        currentHealth = Math.min(maxHealth, currentHealth);
                        healingApplied = true;
                        healingText.setVisible(true);
                        
                        // Update health bar immediately after healing
                        double healthPercentage = Math.max(0, (double) currentHealth / maxHealth);
                        double currentHealthBarWidth = (healthBarWidth - 6) * healthPercentage;
                        healthBarForeground.setWidth(currentHealthBarWidth);
                        
                        // Update health percentage text
                        int healthPercent = (int) Math.round(healthPercentage * 100);
                        healthText.setText(healthPercent + "%");
                        double textWidth = healthText.getLayoutBounds().getWidth();
                        healthText.setX(healthBarX + healthBarWidth / 2 - textWidth / 2);
                    }
                    
                    // After transition duration, spawn next wave
                    if (elapsed >= waveTransitionDuration) {
                        isWaveTransitioning = false;
                        healingText.setVisible(false);
                        currentWave[0]++;
                        waveText.setText("Wave: " + currentWave[0]);
                        // Spawn new wave of enemies
                        spawnWave(currentWave[0], skeletons, skeletonArchers, arrows, skeletonHealthBars, archerHealthBars, root, groundLevel, screenWidth, enemyHealthBarWidth, enemyHealthBarHeight, enemyHealthBarCornerRadius);
                    }
                }
                
                // ========== ENEMY AI AND COLLISION - SKELETONS ==========
                double knightCenterX = character.getX() + character.getFitWidth() / 2.0;

                // Update all skeleton enemies
                for (Skeleton skeleton : skeletons) {
                    // Skip dead skeletons and clean up their HP bars
                    if (!skeleton.isAlive()) {
                        Rectangle[] healthBars = skeletonHealthBars.get(skeleton);
                        if (healthBars != null) {
                            root.getChildren().removeAll(healthBars[0], healthBars[1]);
                            skeletonHealthBars.remove(skeleton);
                        }
                        continue;
                    }
                    
                    // Update skeleton's HP bar position and value
                    Rectangle[] healthBars = skeletonHealthBars.get(skeleton);
                    if (healthBars != null) {
                        double skeletonX = skeleton.getView().getX();
                        double skeletonY = skeleton.getView().getY();
                        double skeletonWidth = skeleton.getView().getFitWidth();
                        
                        // Position HP bar above skeleton
                        double hpBarX = skeletonX + (skeletonWidth - enemyHealthBarWidth) / 2;
                        double hpBarY = skeletonY - 20;
                        
                        healthBars[0].setX(hpBarX);
                        healthBars[0].setY(hpBarY);
                        
                        // Update HP bar width based on skeleton's health percentage
                        double healthPercentage = skeleton.getHealthPercentage();
                        double currentHealthBarWidth = (enemyHealthBarWidth - 2) * healthPercentage;
                        healthBars[1].setX(hpBarX + 1);
                        healthBars[1].setY(hpBarY + 1);
                        healthBars[1].setWidth(Math.max(0, currentHealthBarWidth));
                    }
                    
                    // Apply physics and AI to skeleton
                    skeleton.applyGravity(gravity, groundLevel);
                    int previousRunFrame = skeleton.getCurrentRunFrame();
                    skeleton.chaseAndAttack(knightCenterX, 25, now); // Chase player and attack
                    
                    // Play skeleton walk sounds (commented out)
                    int currentRunFrame = skeleton.getCurrentRunFrame();
                    if (skeleton.isRunning() && currentRunFrame != previousRunFrame && currentRunFrame % 2 == 0) {
                        // playSound(skeleWalkSounds[currentSkeleWalkIndex]);
                        // currentSkeleWalkIndex = (currentSkeleWalkIndex + 1) % 4;
                    }

                    // Check for skeleton attack collision with player
                    if (skeleton.isAttacking() && skeleton.canDealDamage()) {
                        if (character.getBoundsInParent().intersects(skeleton.getView().getBoundsInParent())) {
                            // Player is blocking - block the attack
                            if (isBlocking && !isHurt && !isDead) {
                                // playSound(guardSound);
                                skeleton.markAttackHit();
                            } 
                            // Player is not blocking - take damage
                            else if (!isBlocking && !isHurt && !isDead) {
                                int damageTaken = 10;
                                currentHealth -= damageTaken;
                                currentHealth = Math.max(0, currentHealth);
                                
                                // Check if player died from this attack
                                if (currentHealth <= 0 && !isDead) {
                                    isDead = true;
                                    isAttacking = false;
                                    isDashing = false;
                                    isBlocking = false;
                                    isHurt = false;
                                    isRunning = false;
                                    currentDeathFrame = 0;
                                    lastDeathFrameTime = now;
                                    character.setImage(deathImages[0]);
                                    deathText.applyCss();
                                    double deathTextWidth = deathText.getLayoutBounds().getWidth();
                                    deathText.setX((screenWidth - deathTextWidth) / 2);
                                    deathText.setVisible(true);
                                    
                                    // Play death sounds (commented out)
                                    // playSound(deathSound);
                                    // if (currentMusicPlayer != null) {
                                    //     currentMusicPlayer.stop();
                                    // }
                                    // deathMusicPlayer.play();
                                    // currentMusicPlayer = deathMusicPlayer;
                                    
                                    skeleton.markAttackHit();
                                    continue;
                                }
                                
                                // Play hurt sound (commented out)
                                // playSound(hurtSound);
                                
                                // Create floating damage indicator
                                double characterCenterXPos = character.getX() + character.getFitWidth() / 2.0;
                                double characterTopY = character.getY();
                                DamageIndicator damageIndicator = new DamageIndicator(characterCenterXPos, characterTopY, damageTaken);
                                damageIndicators.add(damageIndicator);
                                root.getChildren().add(damageIndicator.text);
                                damageIndicator.text.applyCss();
                                
                                // Enter hurt state (brief invincibility)
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
                
                // ========== ENEMY AI AND COLLISION - ARCHERS ==========
                // Update all skeleton archer enemies
                for (SkeletonArcher archer : skeletonArchers) {
                    // Skip dead archers and clean up their HP bars
                    if (!archer.isAlive()) {
                        Rectangle[] healthBars = archerHealthBars.get(archer);
                        if (healthBars != null) {
                            root.getChildren().removeAll(healthBars[0], healthBars[1]);
                            archerHealthBars.remove(archer);
                        }
                        continue;
                    }
                    
                    // Update archer's HP bar position and value
                    Rectangle[] healthBars = archerHealthBars.get(archer);
                    if (healthBars != null) {
                        double archerX = archer.getView().getX();
                        double archerY = archer.getView().getY();
                        double archerWidth = archer.getView().getFitWidth();
                        
                        // Position HP bar above archer
                        double hpBarX = archerX + (archerWidth - enemyHealthBarWidth) / 2;
                        double hpBarY = archerY - 20;
                        
                        healthBars[0].setX(hpBarX);
                        healthBars[0].setY(hpBarY);
                        
                        // Update HP bar width based on archer's health percentage
                        double healthPercentage = archer.getHealthPercentage();
                        double currentHealthBarWidth = (enemyHealthBarWidth - 2) * healthPercentage;
                        healthBars[1].setX(hpBarX + 1);
                        healthBars[1].setY(hpBarY + 1);
                        healthBars[1].setWidth(Math.max(0, currentHealthBarWidth));
                    }
                    
                    // Apply physics and AI to archer
                    archer.applyGravity(gravity, groundLevel);
                    archer.chaseAndAttack(knightCenterX, 25, now); //shoot

                    // Spawn arrow when archer is ready to shoot
                    if (archer.shouldShootArrow()) {
                        double archerCenterX = archer.getCenterX();
                        double archerCenterY = archer.getCenterY();
                        int arrowDirection = archer.getDirection();
                        double arrowSpeed = 8.0;
                        
                        // Start arrow from the front edge of the archer
                        double arrowStartX = archerCenterX + (arrowDirection * 100);
                        double arrowStartY = archerCenterY;
                        
                        // Create and add arrow to game
                        Arrow arrow = new Arrow(arrowStartX, arrowStartY, arrowDirection, arrowSpeed);
                        arrows.add(arrow);
                        root.getChildren().add(arrow.getView());
                        archer.markArrowShot();
                        
                        // Play archer shoot sound (commented out)
                        // playSound(archerShootSound);
                    }
                }
                
                // ========== ARROW UPDATE AND COLLISION ==========
                // Update all arrows and check for collisions with player
                List<Arrow> arrowsToRemove = new ArrayList<>();
                for (Arrow arrow : arrows) {
                    arrow.update(screenWidth); // Move arrow and check if it went off screen
                    
                    // Remove arrows that are no longer active (off screen or hit)
                    if (!arrow.isActive()) {
                        arrowsToRemove.add(arrow);
                        root.getChildren().remove(arrow.getView());
                        continue;
                    }
                    
                    // Check if arrow collides with player
                    if (arrow.getView().getBoundsInParent().intersects(character.getBoundsInParent())) {
                        // Player is blocking - block the arrow
                        if (isBlocking && !isHurt && !isDead) {
                            // playSound(guardSound);
                            arrow.deactivate();
                            arrowsToRemove.add(arrow);
                            root.getChildren().remove(arrow.getView());
                        } 
                        // Player is not blocking - take damage
                        else if (!isBlocking && !isHurt && !isDead) {
                            int damageTaken = 10;
                            currentHealth -= damageTaken;
                            currentHealth = Math.max(0, currentHealth);
                            
                            // Check if player died from arrow
                            if (currentHealth <= 0 && !isDead) {
                                isDead = true;
                                isAttacking = false;
                                isDashing = false;
                                isBlocking = false;
                                isHurt = false;
                                isRunning = false;
                                currentDeathFrame = 0;
                                lastDeathFrameTime = now;
                                character.setImage(deathImages[0]);
                                deathText.applyCss();
                                double deathTextWidth = deathText.getLayoutBounds().getWidth();
                                deathText.setX((screenWidth - deathTextWidth) / 2);
                                deathText.setVisible(true);
                                
                                // Play death sounds (commented out)
                                // playSound(deathSound);
                                // if (currentMusicPlayer != null) {
                                //     currentMusicPlayer.stop();
                                // }
                                // deathMusicPlayer.play();
                                // currentMusicPlayer = deathMusicPlayer;
                                
                                arrow.deactivate();
                                arrowsToRemove.add(arrow);
                                root.getChildren().remove(arrow.getView());
                                continue;
                            }
                            
                            // Play hurt sound (commented out)
                            // playSound(hurtSound);
                            
                            // Create floating damage indicator
                            double characterCenterXPos = character.getX() + character.getFitWidth() / 2.0;
                            double characterTopY = character.getY();
                            DamageIndicator damageIndicator = new DamageIndicator(characterCenterXPos, characterTopY, damageTaken);
                            damageIndicators.add(damageIndicator);
                            root.getChildren().add(damageIndicator.text);
                            damageIndicator.text.applyCss();
                            
                            // Enter hurt state
                            isHurt = true;
                            currentHurtFrame = 0;
                            lastHurtFrameTime = now;
                            isAttacking = false; 
                            isDashing = false;
                            character.setOpacity(1.0);
                            character.setImage(hurtImages[0]);
                        }
                        
                        // Arrow always deactivates after hitting player
                        arrow.deactivate();
                        arrowsToRemove.add(arrow);
                        root.getChildren().remove(arrow.getView());
                    }
                }
                arrows.removeAll(arrowsToRemove);


                // ========== PLAYER ANIMATION SYSTEM ==========
                // Check if player is currently moving horizontally
                boolean isCurrentlyRunning = keysPressed.contains(KeyCode.A) || keysPressed.contains(KeyCode.D);
                
                // Priority order
                if (isBlocking) {
                    // Show block animation
                    character.setImage(blockImage);
                }
                else if (isHurt) {
                    // Play hurt animation frames
                    character.setImage(hurtImages[currentHurtFrame]);
                    
                    long elapsed = now - lastHurtFrameTime;
                    if (elapsed >= hurtFrameDuration) {
                        currentHurtFrame++;
                        lastHurtFrameTime = now;
                        
                        // When hurt animation completes, return to appropriate state
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
                    // Determine which attack animation to use (running or standing)
                    boolean useRunningAttack = isCurrentlyRunning || !isOnGround;
                    int maxAttackFrames = useRunningAttack ? runAttackFrameCount : attackFrameCount;
                    
                    // Update attack animation frames
                    long elapsed = now - lastAttackFrameTime;
                    if (elapsed >= attackFrameDuration) {
                        currentAttackFrame++;
                        lastAttackFrameTime = now;
                        
                        // When attack animation completes, return to appropriate state
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
                            // Continue attack animation
                            if (useRunningAttack) {
                                character.setImage(runAttackImages[currentAttackFrame]);
                            } else {
                                character.setImage(attackImages[currentAttackFrame]);
                            }
                        }
                    }
                    
                    // ========== ATTACK DAMAGE DETECTION ==========
                    // Check for hit on enemies during attack (only once per attack)
                    if (!attackHitApplied) {
                        // Check collision with skeletons
                        for (Skeleton skeleton : skeletons) {
                            if (skeleton.isAlive() && character.getBoundsInParent().intersects(skeleton.getView().getBoundsInParent())) {
                                skeleton.hit();
                                attackHitApplied = true;
                                // If skeleton dies, remove it and its HP bar
                                if (!skeleton.isAlive()) {
                                    root.getChildren().remove(skeleton.getView());
                                    Rectangle[] healthBars = skeletonHealthBars.get(skeleton);
                                    if (healthBars != null) {
                                        root.getChildren().removeAll(healthBars[0], healthBars[1]);
                                        skeletonHealthBars.remove(skeleton);
                                    }
                                    // playSound(skeleDeathSound);
                                }
                                break; // Only hit one enemy per attack
                            }
                        }
                        // Check collision with archers if no skeleton was hit
                        if (!attackHitApplied) {
                            for (SkeletonArcher archer : skeletonArchers) {
                                if (archer.isAlive() && character.getBoundsInParent().intersects(archer.getView().getBoundsInParent())) {
                                    archer.hit();
                                    attackHitApplied = true;
                                    // If archer dies, remove it and its HP bar
                                    if (!archer.isAlive()) {
                                        root.getChildren().remove(archer.getView());
                                        Rectangle[] healthBars = archerHealthBars.get(archer);
                                        if (healthBars != null) {
                                            root.getChildren().removeAll(healthBars[0], healthBars[1]);
                                            archerHealthBars.remove(archer);
                                        }
                                        // playSound(skeleDeathSound);
                                    }
                                    break; // Only hit one enemy per attack
                                }
                            }
                        }
                    }  
                }
                else if (!isOnGround) {
                    // Update jump animation while in air
                    long elapsed = now - lastJumpFrameTime;
                    if (elapsed >= jumpFrameDuration) {                    
                        // Advance to next jump frame, but stay on last frame if at end
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
                    // Update running animation while moving horizontally
                    long elapsed = now - lastRunFrameTime;
                    if (elapsed >= runFrameDuration) {
                        currentRunFrame = (currentRunFrame + 1) % runFrameCount; // Loop animation
                        lastRunFrameTime = now;
                        character.setImage(runImages[currentRunFrame]);
                    }
                }
                

                // ========== DASH MECHANICS ==========
                // Start dash if SHIFT is pressed, not already dashing, not hurt, and cooldown has passed
                if (keysPressed.contains(KeyCode.SHIFT) && !isDashing && !isHurt && (now - lastDashTime >= dashCooldown)) {
                    isDashing = true;
                    dashProgress = 0;
                    dashDirection = facingRight[0] ? 1 : -1;
                    dashInvisibleFrames = 2;
                    lastDashTime = now;
                    // playSound(dashSound);
                }
                
                // Update dash movement and effects
                if (isDashing) {
                    // Make character invisible for first few frames (invincibility)
                    if (dashInvisibleFrames > 0) {
                        character.setOpacity(0.0);
                        dashInvisibleFrames--;
                    } else {
                        character.setOpacity(1.0);
                    }
                    
                    // Create dash afterimage effect
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
                    
                    // Move character during dash
                    double dashMove = dashSpeed;
                    double newX = character.getX() + (dashMove * dashDirection);
                    
                    // Update character position
                    character.setX(newX);
                    dashProgress += dashMove;
                    
                    // End dash when distance is reached
                    if (dashProgress >= dashDistance) {
                        isDashing = false;
                        dashProgress = 0;
                        dashAfterimageCounter = 0;
                        character.setOpacity(1.0); 
                    }
                }
                
                // ========== DASH AFTERIMAGE FADE ==========
                // Fade out and remove dash afterimages
                List<ImageView> toRemove = new ArrayList<>();
                for (ImageView afterimage : afterimages) {
                    double currentOpacity = afterimage.getOpacity();
                    currentOpacity -= 0.1;
                    afterimage.setOpacity(currentOpacity);
                    
                    // Remove when fully faded
                    if (currentOpacity <= 0) {
                        toRemove.add(afterimage);
                        root.getChildren().remove(afterimage);
                    }
                }
                afterimages.removeAll(toRemove);
                

                // ========== HORIZONTAL MOVEMENT ==========
                // Handle left/right movement (only when not dashing, blocking, or hurt)
                if (!isDashing && !isBlocking && !isHurt) {
                    boolean wasRunning = isRunning;
                    isRunning = false;
                    
                    // Move left (A key)
                    if (keysPressed.contains(KeyCode.A)) {
                        isRunning = true;
                        // Flip sprite if facing right
                        if (facingRight[0]) {
                            character.setX(character.getX() - 65); // Adjust for flip
                            character.setScaleX(-1); // Flip horizontally
                            facingRight[0] = false;
                        }
                        character.setX(character.getX() - characterSpeed);
                    }
                    // Move right (D key)
                    if (keysPressed.contains(KeyCode.D)) {
                        isRunning = true;
                        // Flip sprite if facing left
                        if (!facingRight[0]) {
                            character.setX(character.getX() + 65); // Adjust for flip
                            character.setScaleX(1); // Normal orientation
                            facingRight[0] = true;
                        }
                        character.setX(character.getX() + characterSpeed);
                    }
                    
                    // Play footstep sound when running (commented out)
                    // if (isRunning && isOnGround && (now - lastFootstepTime >= footstepInterval)) {
                    //     playSound(footstepSound);
                    //     lastFootstepTime = now;
                    // }
                    
                    // Handle animation transitions when starting/stopping running
                    if (!isAttacking && !isHurt) {
                        // Stopped running - return to idle
                        if (wasRunning && !isRunning) {
                            character.setImage(characterImage);
                            currentRunFrame = 0;
                        }
                        // Started running - begin run animation
                        else if (!wasRunning && isRunning) {
                            currentRunFrame = 0;
                            lastRunFrameTime = now;
                            character.setImage(runImages[0]);
                        }
                    }
                }
                
                // ========== JUMP MECHANICS ==========
                // Handle jump input (SPACE or W key)
                if (jumpKeyPressed && !isBlocking && !isHurt) {
                    // Regular jump from ground
                    if (isOnGround) {
                        velocityY = jumpStrength;
                        isOnGround = false;
                        hasDoubleJump = true; // Enable double jump
                        currentJumpFrame = 0;
                        lastJumpFrameTime = System.nanoTime();
                        character.setImage(jumpImages[0]);
                        jumpKeyPressed = false;
                        // playSound(jumpSound);
                    } 
                    // Double jump in air
                    else if (hasDoubleJump) {
                        velocityY = jumpStrength;
                        hasDoubleJump = false; // Disable further jumps
                        currentJumpFrame = 0;
                        lastJumpFrameTime = System.nanoTime();
                        character.setImage(jumpImages[0]);
                        jumpKeyPressed = false;
                        // playSound(jumpSound);
                    } 
                    // No jump available
                    else {
                        jumpKeyPressed = false;
                    }
                }
            
                // ========== GRAVITY AND VERTICAL MOVEMENT ==========
                // Apply gravity when not on ground
                if (!isOnGround) {
                    velocityY += gravity; // Increase downward velocity
                }

                // Calculate new Y position
                double newY = character.getY() + velocityY;
                
                // Check ground collision
                if (newY >= groundLevel) {
                    newY = groundLevel;
                    velocityY = 0;
                    isOnGround = true;
                    hasDoubleJump = true; // Reset double jump
                    // Set appropriate animation when landing
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
                
                // ========== SCREEN WRAP-AROUND ==========
                // Wrap character to opposite side when going off screen edges
                double characterWidth = character.getFitWidth();
                double currentX = character.getX();
                if (currentX + characterWidth < 0) {
                    // Went off left edge - wrap to right
                    character.setX(screenWidth - characterWidth);
                } else if (currentX > screenWidth) {
                    // Went off right edge - wrap to left
                    character.setX(0);
                }
                
                // ========== UPDATE DAMAGE INDICATORS ==========
                // Update and remove expired damage indicators (floating damage numbers)
                List<DamageIndicator> indicatorsToRemove = new ArrayList<>();
                for (DamageIndicator indicator : damageIndicators) {
                    if (!indicator.update(now)) {
                        root.getChildren().remove(indicator.text);
                        indicatorsToRemove.add(indicator);
                    }
                }
                damageIndicators.removeAll(indicatorsToRemove);

                // ========== UPDATE HEALTH BAR UI ==========
                // Update health bar width and percentage text based on current health
                double healthPercentage = Math.max(0, (double) currentHealth / maxHealth);
                double currentHealthBarWidth = (healthBarWidth - 6) * healthPercentage;
                healthBarForeground.setWidth(currentHealthBarWidth);

                // Update health percentage text
                int healthPercent = (int) Math.round(healthPercentage * 100);
                healthText.setText(healthPercent + "%");
                double textWidth = healthText.getLayoutBounds().getWidth();
                healthText.setX(healthBarX + healthBarWidth / 2 - textWidth / 2);
            }
        };
        gameTimer.start(); // Start the main game loop

        stage.setScene(scene);
        stage.setTitle("Fallen Knight");
        stage.show();

        root.requestFocus();
    }

    /**
     * Configuration class for wave enemy composition
     * Stores how many skeletons and archers should spawn in a wave
     */
    private static class WaveConfig {
        int skeletons; // Number of melee skeleton enemies
        int archers;   // Number of ranged skeleton archer enemies
        
        WaveConfig(int skeletons, int archers) {
            this.skeletons = skeletons;
            this.archers = archers;
        }
    }
    
    /**
     * Wave configurations array - defines enemy composition for each wave
     * Format: (Skeletons, Archers)
     */
    private static WaveConfig[] waveConfigs = {
        new WaveConfig(1, 0),
        new WaveConfig(1, 1),
        new WaveConfig(2, 0),
        new WaveConfig(2, 1),
        new WaveConfig(3, 1),
        new WaveConfig(2, 2),
        new WaveConfig(3, 2),
        new WaveConfig(4, 0),
        new WaveConfig(4, 1),
        new WaveConfig(4, 2),
        new WaveConfig(4, 3),
        new WaveConfig(4, 4),
        new WaveConfig(5, 2),
        new WaveConfig(5, 4),
        new WaveConfig(5, 5),
    };
    
    /**
     * Spawns a new wave of enemies, removing the previous wave
     * Positions enemies across the screen and creates health bars for each
     */
    private static void spawnWave(int waveNumber, List<Skeleton> skeletons, List<SkeletonArcher> skeletonArchers, List<Arrow> arrows, Map<Skeleton, Rectangle[]> skeletonHealthBars, Map<SkeletonArcher, Rectangle[]> archerHealthBars, Pane root, double groundLevel, double screenWidth, double enemyHealthBarWidth, double enemyHealthBarHeight, double enemyHealthBarCornerRadius) {
        // ========== CLEANUP PREVIOUS WAVE ==========
        // Remove old skeletons and their HP bars
        for (Skeleton skeleton : skeletons) {
            root.getChildren().remove(skeleton.getView());
            Rectangle[] healthBars = skeletonHealthBars.get(skeleton);
            if (healthBars != null) {
                root.getChildren().removeAll(healthBars[0], healthBars[1]);
            }
        }
        skeletons.clear();
        skeletonHealthBars.clear();
        
        // Remove old archers and their HP bars
        for (SkeletonArcher archer : skeletonArchers) {
            root.getChildren().remove(archer.getView());
            Rectangle[] healthBars = archerHealthBars.get(archer);
            if (healthBars != null) {
                root.getChildren().removeAll(healthBars[0], healthBars[1]);
            }
        }
        skeletonArchers.clear();
        archerHealthBars.clear();
        
        // Remove all arrows from previous wave
        for (Arrow arrow : arrows) {
            root.getChildren().remove(arrow.getView());
        }
        arrows.clear();
        
        // ========== DETERMINE WAVE CONFIGURATION ==========
        // Get wave configuration from array, or create default for waves beyond array
        WaveConfig config;
        if (waveNumber <= waveConfigs.length) {
            config = waveConfigs[waveNumber - 1];
        } else {
            config = new WaveConfig(waveNumber, 0);
        }
        
        // ========== ENEMY POSITIONING ==========
        double skeletonWidth = 200;
        double spacing = 250;
        double startX = 900;
        int enemyIndex = 0;

        // ========== SPAWN SKELETONS ==========
        for (int i = 0; i < config.skeletons; i++) {
            double xPos = startX + (enemyIndex * spacing);
            while (xPos > screenWidth - skeletonWidth) {
                xPos = xPos - (screenWidth - skeletonWidth) + 100;
            }
            if (xPos < 400) {
                xPos = 400;
            }
            
            // Create skeleton at calculated position
            Skeleton skeleton = new Skeleton(xPos, groundLevel);
            skeletons.add(skeleton);
            root.getChildren().add(skeleton.getView());
            
            // Create HP bar background
            Rectangle skeletonHealthBarBg = new Rectangle(0, 0, enemyHealthBarWidth, enemyHealthBarHeight);
            skeletonHealthBarBg.setFill(Color.rgb(40, 40, 40));
            skeletonHealthBarBg.setStroke(Color.rgb(20, 20, 20));
            skeletonHealthBarBg.setStrokeWidth(1);
            skeletonHealthBarBg.setArcWidth(enemyHealthBarCornerRadius);
            skeletonHealthBarBg.setArcHeight(enemyHealthBarCornerRadius);
            
            // Create HP bar foreground
            Rectangle skeletonHealthBarFg = new Rectangle(1, 1, enemyHealthBarWidth - 2, enemyHealthBarHeight - 2);
            skeletonHealthBarFg.setFill(Color.RED);
            skeletonHealthBarFg.setArcWidth(enemyHealthBarCornerRadius - 1);
            skeletonHealthBarFg.setArcHeight(enemyHealthBarCornerRadius - 1);
            
            // Add HP bars to scene and store reference
            root.getChildren().addAll(skeletonHealthBarBg, skeletonHealthBarFg);
            skeletonHealthBars.put(skeleton, new Rectangle[]{skeletonHealthBarBg, skeletonHealthBarFg});
            
            enemyIndex++;
        }

        // ========== SPAWN ARCHERS ==========
        for (int i = 0; i < config.archers; i++) {
            double xPos = startX + (enemyIndex * spacing);
            while (xPos > screenWidth - skeletonWidth) {
                xPos = xPos - (screenWidth - skeletonWidth) + 100;
            }
            if (xPos < 400) {
                xPos = 400;
            }
            
            // Create archer at calculated position
            SkeletonArcher archer = new SkeletonArcher(xPos, groundLevel);
            skeletonArchers.add(archer);
            root.getChildren().add(archer.getView());
            
            // Create HP bar background
            Rectangle archerHealthBarBg = new Rectangle(0, 0, enemyHealthBarWidth, enemyHealthBarHeight);
            archerHealthBarBg.setFill(Color.rgb(40, 40, 40));
            archerHealthBarBg.setStroke(Color.rgb(20, 20, 20));
            archerHealthBarBg.setStrokeWidth(1);
            archerHealthBarBg.setArcWidth(enemyHealthBarCornerRadius);
            archerHealthBarBg.setArcHeight(enemyHealthBarCornerRadius);
            
            // Create HP bar foreground
            Rectangle archerHealthBarFg = new Rectangle(1, 1, enemyHealthBarWidth - 2, enemyHealthBarHeight - 2);
            archerHealthBarFg.setFill(Color.RED);
            archerHealthBarFg.setArcWidth(enemyHealthBarCornerRadius - 1);
            archerHealthBarFg.setArcHeight(enemyHealthBarCornerRadius - 1);
            
            // Add HP bars to scene and store reference
            root.getChildren().addAll(archerHealthBarBg, archerHealthBarFg);
            archerHealthBars.put(archer, new Rectangle[]{archerHealthBarBg, archerHealthBarFg});
            
            enemyIndex++; // Increment for next enemy positioning
        }
    }

    /**
     * Class for floating damage indicators that appear when player takes damage
     */
    private static class DamageIndicator {
        Text text;
        double x;
        double y;
        double opacity;
        long startTime;
        static final long LIFETIME = 1_000_000_000L;
        static final double FLOAT_SPEED = 0.8;
        
        /**
         * Creates a new damage indicator at the specified position
         */
        DamageIndicator(double x, double y, int damage) {
            this.x = x;
            this.y = y + 150; // Offset upward from character
            this.opacity = 1.0;
            this.startTime = System.nanoTime();
            
            // Create red damage text
            this.text = new Text("-" + damage);
            this.text.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            this.text.setFill(Color.RED);

            this.text.setX(x);
            this.text.setY(y);
        }
        
        /**
         * Updates the damage indicator position and opacity
         * @param now Current time in nanoseconds
         * @return true if indicator is still active, false if it should be removed
         */
        boolean update(long now) {
            long elapsed = now - startTime;
            
            // Float upward
            y -= FLOAT_SPEED;
            text.setY(y);
            
            // Center text horizontally
            double textWidth = text.getLayoutBounds().getWidth();
            text.setX(x - textWidth / 2);
            
            // Fade out during second half of lifetime
            if (elapsed > LIFETIME / 2) {
                double fadeProgress = (elapsed - LIFETIME / 2.0) / (LIFETIME / 2.0);
                opacity = Math.max(0, 1.0 - fadeProgress);
                Color currentColor = Color.color(1.0, 0.0, 0.0, opacity);
                text.setFill(currentColor);
            }
            
            // Return false when lifetime expires or fully faded
            return elapsed < LIFETIME && opacity > 0;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}