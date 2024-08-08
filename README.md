# TetrECS - JavaFX-Based Block Placement Game
This project is part of a major coursework assignment for my Programming 2 module, with an **ACHIEVED GRADE: 77/80**. The coursework involved designing and implementing a fast-paced block placement game called TetrECS, using JavaFX and various Java programming concepts. The game will not work outside of the university, due to the communicator being used for the multiplayer aspect.

## Key Features and Implementations:
- Game Logic: Developed the core game mechanics, including piece placement, line clearing (horizontally and vertically), score calculation, and level progression. Implemented a scoring multiplier system for consecutive line clears and a life management system where the player loses a life for each failed block placement.

- Custom Components: Utilized JavaFX to create custom UI components such as GameBlock, a custom Canvas component representing individual blocks, and GameBoard, a custom GridPane that holds and displays these blocks dynamically.

- Graphics and Animation: Enhanced the user experience by adding animations and custom graphics to the game. Implemented smooth transitions for piece rotations, movements, and line clearing effects.

- Event Handling and Binding: Leveraged JavaFX’s property binding and event listeners to handle user interactions, including piece rotation, storage, and placement. Integrated these interactions with the game logic to ensure real-time updates to the game state.

- Game Loop: Designed a game loop that manages the game's timing and difficulty scaling. As the player's score increases, the game loop adjusts to give the player less time to place pieces, adding to the challenge.

- Networking and Multiplayer: Implemented networking capabilities to support multiplayer functionality. Utilized a dedicated server to manage game sessions, player connections, and real-time score updates.

- Online Scoreboard: Created an online scoreboard system where players' scores are uploaded and ranked globally. Integrated this feature with the multiplayer mode to foster competition.

- User Interface: Designed and built an intuitive and responsive user interface, including a main menu, game scenes, instructions, and multiplayer lobby. Ensured the UI scales properly across different screen sizes.

- Customization and Assets: Provided support for custom themes and assets, allowing players to personalize their gaming experience. Included various sound effects, background music, and visual styles.

## Technologies Used:
- JavaFX: For building the user interface and handling graphical components.
- Java: Core programming for game logic, event handling, and networking.
- Networking: Implemented using a custom server to handle multiplayer connections and communications.
- Git: Version control throughout the development process.
- This project demonstrates my ability to apply software development principles in a complex and dynamic application, using JavaFX and Java. It also showcases my skills in problem-solving, game development, UI/UX design, and network programming.
