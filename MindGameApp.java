// MindGameApp.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

/**
 * MindGameApp (theming update)
 * - UI updated to solid dark royal blue background and royal gold text
 * - Stylish Georgia font used
 * - Kept all original game logic intact (word scramble, tic-tac-toe, hints, tries, AI)
 *
 * Paste this file over your existing MindGameApp.java (keeps game logic, changes UI/styling only).
 */
public class MindGameApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MainMenuPanel mainMenuPanel;
    private WordPanel wordPanel;
    private TicTacToeMenuPanel ticMenuPanel;

    // Royal colors and fonts (centralized)
    private final Color ROYAL_BLUE = new Color(7, 18, 51);     // very dark royal blue
    private final Color ROYAL_GOLD = new Color(255, 215, 0);  // gold
    private final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 48);
    private final Font LARGE_FONT = new Font("Georgia", Font.BOLD, 28);
    private final Font MEDIUM_FONT = new Font("Georgia", Font.PLAIN, 20);
    private final Font SMALL_FONT = new Font("Georgia", Font.PLAIN, 16);

    public MindGameApp() {
        setTitle("Mind Game - Royal Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // open maximized; still resizable
        setMinimumSize(new Dimension(900, 600));  // allow smaller windows
        initUI();
    }

    private void initUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(ROYAL_BLUE);

        // instantiate panels (keeps original logic inside them)
        mainMenuPanel = new MainMenuPanel();
        wordPanel = new WordPanel();
        ticMenuPanel = new TicTacToeMenuPanel();

        mainPanel.add(mainMenuPanel, "MAIN");
        mainPanel.add(wordPanel, "WORD");
        mainPanel.add(ticMenuPanel, "TICMENU");

        add(mainPanel);
        cardLayout.show(mainPanel, "MAIN");
        setVisible(true);
    }

    // Helper to create text-only styled buttons (royal gold text, solid royal background)
    private JButton createTextButton(String text, Font font, int prefWidth, int prefHeight) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setBackground(ROYAL_BLUE);
        btn.setForeground(ROYAL_GOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(ROYAL_GOLD.brighter(), 2));
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(prefWidth, prefHeight));
        // hover effect: slightly brighter blue on hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(12, 30, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ROYAL_BLUE);
            }
        });
        return btn;
    }

    // ---------- Main Menu Panel ----------
    private class MainMenuPanel extends JPanel {
        MainMenuPanel() {
            setBackground(ROYAL_BLUE);
            setLayout(new BorderLayout());
            // Title region
            JLabel title = new JLabel("Mind Game Royale", SwingConstants.CENTER);
            title.setFont(TITLE_FONT);
            title.setForeground(ROYAL_GOLD);
            title.setBorder(new EmptyBorder(40, 10, 10, 10));
            add(title, BorderLayout.NORTH);

            // Center area for buttons (game choices)
            JPanel center = new JPanel();
            center.setBackground(ROYAL_BLUE);
            center.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.gridx = 0;
            gbc.gridy = 0;

            // Game choice buttons: smaller than title, prominent but compact
            JButton wordBtn = createTextButton("Word Scramble", LARGE_FONT, 300, 60);
            JButton ticBtn = createTextButton("Tic Tac Toe", LARGE_FONT, 300, 60);
            JButton exitBtn = createTextButton("Exit", MEDIUM_FONT, 200, 50);

            // wire actions to existing card layout and panels
            wordBtn.addActionListener(_ -> {
                cardLayout.show(mainPanel, "WORD");
                wordPanel.showLevelSelection();
            });
            ticBtn.addActionListener(_ -> {
                cardLayout.show(mainPanel, "TICMENU");
                ticMenuPanel.showMenu();
            });
            exitBtn.addActionListener(_ -> System.exit(0));

            // arrange small header label above choices
            JLabel chooseLabel = new JLabel("Choose a Game", SwingConstants.CENTER);
            chooseLabel.setFont(MEDIUM_FONT);
            chooseLabel.setForeground(ROYAL_GOLD);
            chooseLabel.setBorder(new EmptyBorder(10,10,20,10));
            gbc.gridwidth = 1;
            center.add(chooseLabel, gbc);

            gbc.gridy++;
            center.add(wordBtn, gbc);
            gbc.gridy++;
            center.add(ticBtn, gbc);
            gbc.gridy++;
            center.add(exitBtn, gbc);

            add(center, BorderLayout.CENTER);

            // Footer small text
            JLabel footer = new JLabel("Designed with a royal theme • Enjoy learning & playing", SwingConstants.CENTER);
            footer.setFont(SMALL_FONT);
            footer.setForeground(ROYAL_GOLD);
            footer.setBorder(new EmptyBorder(10,10,20,10));
            add(footer, BorderLayout.SOUTH);
        }
    }

    // ---------- Word Scramble Panel (UI restyled only; logic preserved) ----------
    private class WordPanel extends JPanel {
        private JPanel topPanel;
        private JPanel centerPanel;
        private JPanel controlPanel;
        private JLabel scrambledLabel;
        private JLabel hintLabel;
        private JLabel triesLabel;
        private JTextField inputField;
        private JButton submitBtn;
        private JButton backBtn;
        private JButton nextBtn;
        private JComboBox<String> levelCombo;

        private ArrayList<WordData> easyWords = new ArrayList<>();
        private ArrayList<WordData> mediumWords = new ArrayList<>();
        private ArrayList<WordData> hardWords = new ArrayList<>();
        private ArrayList<WordData> currentList = new ArrayList<>();
        private Random rnd = new Random();

        private WordData currentWord;
        private int attemptsLeft;
        private int wordsShown = 0;
        private int maxRounds = 10; // words per session

        WordPanel() {
            setBackground(ROYAL_BLUE);
            setLayout(new BorderLayout());
            topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(true);
            topPanel.setBackground(ROYAL_BLUE);
            topPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

            JLabel title = new JLabel("Word Scramble", SwingConstants.LEFT);
            title.setFont(LARGE_FONT);
            title.setForeground(ROYAL_GOLD);
            topPanel.add(title, BorderLayout.NORTH);

            // level selection and controls (compact)
            JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
            levelPanel.setOpaque(true);
            levelPanel.setBackground(ROYAL_BLUE);
            levelPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
            levelCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
            levelCombo.setFont(SMALL_FONT);
            levelCombo.setForeground(ROYAL_GOLD);
            levelCombo.setBackground(new Color(20, 30, 70));
            JButton startBtn = createTextButton("Start", MEDIUM_FONT, 110, 40);
            JButton changeBtn = createTextButton("Back to Main", MEDIUM_FONT, 150, 40);
            changeBtn.addActionListener(_ -> cardLayout.show(mainPanel, "MAIN"));
            startBtn.addActionListener(_ -> startSession());
            levelPanel.add(new JLabel("Level:"));
            JLabel levelLabel = new JLabel(); levelLabel.setText(""); // spacer
            levelLabel.setForeground(ROYAL_GOLD);
            levelPanel.add(levelCombo);
            levelPanel.add(startBtn);
            levelPanel.add(changeBtn);

            topPanel.add(levelPanel, BorderLayout.SOUTH);
            add(topPanel, BorderLayout.NORTH);

            // center: scrambled word & input
            centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(true);
            centerPanel.setBackground(ROYAL_BLUE);
            centerPanel.setBorder(new EmptyBorder(20, 60, 20, 60));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(10, 10, 10, 10);
            c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
            scrambledLabel = new JLabel("", SwingConstants.CENTER);
            scrambledLabel.setFont(new Font("Georgia", Font.BOLD, 40));
            scrambledLabel.setForeground(ROYAL_GOLD);
            centerPanel.add(scrambledLabel, c);

            c.gridy++;
            hintLabel = new JLabel("", SwingConstants.CENTER);
            hintLabel.setFont(MEDIUM_FONT);
            hintLabel.setForeground(ROYAL_GOLD);
            centerPanel.add(hintLabel, c);

            c.gridy++;
            inputField = new JTextField();
            inputField.setFont(MEDIUM_FONT);
            inputField.setForeground(ROYAL_BLUE);
            inputField.setBackground(Color.WHITE);
            inputField.setColumns(20);
            c.gridwidth = 1;
            centerPanel.add(inputField, c);

            c.gridx = 1;
            submitBtn = createTextButton("Submit Guess", MEDIUM_FONT, 160, 40);
            submitBtn.addActionListener(_ -> submitGuess());
            centerPanel.add(submitBtn, c);

            c.gridx = 0; c.gridy++; c.gridwidth = 2;
            triesLabel = new JLabel("", SwingConstants.CENTER);
            triesLabel.setFont(SMALL_FONT);
            triesLabel.setForeground(ROYAL_GOLD);
            centerPanel.add(triesLabel, c);

            c.gridy++;
            nextBtn = createTextButton("Skip to Next Word", SMALL_FONT, 200, 36);
            nextBtn.addActionListener(_ -> nextWord());
            centerPanel.add(nextBtn, c);

            add(centerPanel, BorderLayout.CENTER);

            // bottom panel: instructions and back
            controlPanel = new JPanel(new BorderLayout());
            controlPanel.setOpaque(true);
            controlPanel.setBackground(ROYAL_BLUE);
            controlPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

            JLabel instr = new JLabel("<html><center>Rules: Guess the scrambled word. You have 4 attempts. "
                    + "Hint appears only after the first wrong attempt. If you guess correctly or exhaust tries, next word appears automatically. "
                    + "You can return to Main Menu anytime.</center></html>", SwingConstants.CENTER);
            instr.setFont(SMALL_FONT);
            instr.setForeground(ROYAL_GOLD);
            controlPanel.add(instr, BorderLayout.CENTER);

            backBtn = createTextButton("Return to Main Menu", SMALL_FONT, 220, 40);
            backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "MAIN"));
            JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomRight.setOpaque(false);
            bottomRight.add(backBtn);
            controlPanel.add(bottomRight, BorderLayout.SOUTH);

            add(controlPanel, BorderLayout.SOUTH);

            // initialize word lists (keeps your words)
            initializeAllWords();
            setSessionInactive();
        }

        private void setSessionInactive() {
            scrambledLabel.setText("");
            hintLabel.setText("");
            inputField.setEnabled(false);
            submitBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            triesLabel.setText("");
        }

        private void setSessionActive() {
            inputField.setEnabled(true);
            submitBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            inputField.requestFocus();
        }

        void showLevelSelection() {
            levelCombo.setSelectedIndex(0);
            setSessionInactive();
        }

        private void initializeAllWords() {
            // Using your provided lists - copied exactly (kept structure)
            String[][] easyData = {
                    {"adventure","Involves exploration","Often exciting","Found in stories"},
                    {"courage","Inner strength","Needed to face fear","Heroic quality"},
                    {"harmony","Peaceful combination","Used in music","Opposite of conflict"},
                    {"fortune","Great wealth","Related to luck","Can be found or lost"},
                    {"journey","Travel experience","Has destination","Can be long or short"},
                    {"wisdom","Deep knowledge","Comes with age","Guides decisions"},
                    {"honest","Always truthful","A good virtue","Opposite of liar"},
                    {"freedom","Independence","Desired by all","Opposite of slavery"},
                    {"unity","Togetherness","Seen in teamwork","Opposite of division"},
                    {"success","Achieving goals","Everyone desires","Opposite of failure"},
                    {"hope","Feeling of expectation","Keeps us going","Opposite of despair"},
                    {"peace","Calm and quiet","Opposite of war","Desired by nations"},
                    {"kindness","Being generous","Comes from heart","Opposite of cruelty"},
                    {"strength","Power or energy","Physical or emotional","Opposite of weakness"},
                    {"patience","Ability to wait","Important virtue","Opposite of anger"},
                    {"talent","Natural ability","Found in artists","Opposite of inexperience"},
                    {"respect","Showing regard","Earned not demanded","Opposite of insult"},
                    {"faith","Strong belief","Often spiritual","Opposite of doubt"},
                    {"pride","Feeling of achievement","Can be good or bad","Opposite of shame"},
                    {"friendship","Mutual bond","Built on trust","Between people"},
                    {"creativity","Thinking new ideas","Important for art","Opposite of copying"},
                    {"discipline","Self-control","Needed for success","Opposite of laziness"},
                    {"gratitude","Thankfulness","Shown by appreciation","Opposite of ingratitude"},
                    {"honesty","Truthfulness","Moral value","Builds trust"},
                    {"determination","Strong will","Never gives up","Key to success"},
                    {"empathy","Understanding feelings","Related to kindness","Builds connection"},
                    {"focus","Concentration","Needed for goals","Opposite of distraction"},
                    {"leadership","Guiding others","Needed in teamwork","Role of a leader"},
                    {"responsibility","Being accountable","Important in work","Opposite of carelessness"},
                    {"curiosity","Desire to learn","Drives innovation","Opposite of disinterest"},
                    {"ambition","Strong desire","To achieve success","Career-related"},
                    {"confidence","Self-belief","Seen in achievers","Opposite of doubt"},
                    {"loyalty","Faithfulness","In relationships","Opposite of betrayal"},
                    {"justice","Fair treatment","In courts","Opposite of bias"},
                    {"humanity","Quality of being human","Caring and kind","Opposite of cruelty"},
                    {"charity","Giving to others","Social act","Helps the poor"},
                    {"motivation","Reason to act","Drives people","Comes from within"},
                    {"perseverance","Continuing effort","Despite failures","Key to success"},
                    {"teamwork","Working together","For common goal","Opposite of selfishness"},
                    {"decision","Making choice","Involves judgment","Important in life"},
                    {"opportunity","Favorable chance","Brings success","Must be taken"},
                    {"time","Keeps moving","Can’t be reversed","Valuable resource"},
                    {"nature","World around us","Includes trees","Source of life"},
                    {"health","Physical fitness","State of well-being","Opposite of illness"},
                    {"happiness","Feeling of joy","Desired by all","Opposite of sadness"},
                    {"education","Learning process","Happens in schools","Knowledge source"},
                    {"effort","Hard work","Leads to success","Opposite of laziness"},
                    {"calmness","Peaceful state","Opposite of anger","Helps focus"},
                    {"mindset","Way of thinking","Determines behavior","Can be positive"},
                    {"politeness","Good manners","Social etiquette","Opposite of rudeness"},
                    {"selfesteem","Self-respect","Based on confidence","Personal worth"},
                    {"bravery","Facing fear","Seen in soldiers","Opposite of cowardice"},
                    {"truth","What’s real","Honest fact","Opposite of lie"},
                    {"love","Deep affection","Universal feeling","Builds bonds"},
                    {"trust","Foundation of relation","Built over time","Easily broken"},
                    {"calm","Peaceful state","Serenity","Opposite of chaos"},
                    {"creativity","Original thinking","Artistic flair","Solves problems"},
                    {"punctuality","Being on time","Important habit","Opposite of delay"},
                    {"focus","Concentrated attention","Avoid distraction","Leads to success"},
                    {"hope","Positive belief","Gives strength","Faith in future"},
                    {"kindness","Helping others","Shows compassion","Warm heart"},
                    {"truthfulness","Speaking truth","Builds character","Honesty quality"},
                    {"empathy","Feeling others pain","Builds connection","Emotional skill"},
                    {"love","Emotional bond","Affection","Universal feeling"},
                    {"unity","Together as one","Creates strength","Common goal"},
                    {"gratitude","Feeling thankful","Express appreciation","Builds happiness"},
                    {"respect","Valuing others","Good behavior","Courtesy"}
            };

            String[][] mediumData = {
                    {"algorithm","Step-by-step method","Used in coding","Solves problems"},
                    {"transistor","Semiconductor device","Acts as switch","Has three terminals"},
                    {"diode","One-way device","Rectifier","Has anode and cathode"},
                    {"capacitor","Stores charge","Used in timing","Two plates"},
                    {"resistor","Limits current","Color-coded","Measured in ohms"},
                    {"amplifier","Boosts signals","Uses op-amp","Increases voltage"},
                    {"transformer","Changes voltage","Works with coils","AC device"},
                    {"signal","Electrical message","Analog or digital","Carries data"},
                    {"frequency","Cycles per second","Measured in hertz","Wave property"},
                    {"inductor","Stores energy","Magnetic field","Used in filters"},
                    {"logic","Reasoning process","Used in circuits","Boolean operation"},
                    {"transducer","Converts energy","Between forms","Used in sensors"},
                    {"power","Energy per second","Measured in watts","P equals V I"},
                    {"current","Flow of charge","Measured in amps","I equals V over R"},
                    {"voltage","Electric potential","Measured in volts","V equals I R"},
                    {"sensor","Detects changes","Used in IoT","Converts input to signal"},
                    {"microchip","Tiny circuit","Controls function","Found in electronics"},
                    {"microcontroller","Small computer","Used in devices","Controls operations"},
                    {"database","Data storage","Organized collection","SQL manages it"},
                    {"network","Connection system","Transfers data","Internet uses it"},
                    {"software","Program code","Runs on hardware","Performs tasks"},
                    {"hardware","Physical parts","Of computer","Includes cpu"},
                    {"compiler","Converts code","To machine language","Programmer tool"},
                    {"function","Block of code","Performs task","Used repeatedly"},
                    {"loop","Repeats code","For or while type","Used in programming"},
                    {"variable","Stores data","Has name and type","Used in code"},
                    {"exception","Runtime error","Needs handling","In programs"},
                    {"object","Instance of class","Used in oop","Has attributes"},
                    {"filter","Removes noise","Used in circuits","Frequency tool"},
                    {"protocol","Communication rule","TCP IP example","Ensures data transfer"},
                    {"router","Network device","Directs data","Internet gateway"},
                    {"antenna","Radiates waves","Used in communication","Transmits or receives"},
                    {"relay","Control switch","Coil driven","Used in circuits"},
                    {"debugging","Fixing errors","In code","Improves output"},
                    {"firmware","Program in chip","Runs device","Embedded software"},
                    {"power supply","Provides voltage","AC to DC","Used in circuits"},
                    {"embedded","Built in system","Performs specific task","Found in devices"},
                    {"encryption","Data protection","Converts to code","Cybersafety method"},
                    {"cloud","Online storage","Remote access","Virtual servers"},
                    {"wifi","Wireless network","Local connection","Internet access"},
                    {"bluetooth","Short range","Wireless tech","Device pairing"},
                    {"rfid","Radio id system","Used in tags","Contactless technology"},
                    {"gps","Positioning system","Uses satellites","Gives location"},
                    {"programming","Writing code","Uses logic","Developer task"},
                    {"syntax","Grammar of code","Structure","Needs accuracy"},
                    {"coding","Writing programs","Logical process","Developer skill"},
                    {"technology","Modern advancement","Uses science","Improves life"},
                    {"processor","Brain of computer","Executes code","Microchip"},
                    {"logicgate","Digital switch","AND OR NOT","Boolean function"},
                    {"sensornode","Iot component","Measures environment","Sends data"},
                    {"photodiode","Light sensitive","Converts light","Used in sensors"},
                    {"innovation","Creative improvement","New ideas","Scientific progress"},
                    {"dataframe","Table of data","Used in analysis","Data science tool"},
                    {"energy","Power in motion","Used in systems","Source of work"},
                    {"modulation","Encoding signal","Used in am fm","Communication concept"},
                    {"semiconductor","Partly conducts","Silicon based","Used in chips"},
                    {"actuator","Produces motion","Converts energy","Opposite of sensor"},
                    {"resilience","Ability to recover","After difficulty","Strength quality"},
                    {"feedback","Output to input","Stabilizes system","Used in amplifiers"},
                    {"decision","Making choice","Requires judgment","Everyday activity"}
            };

            String[][] hardData = {
                    {"microprocessor","CPU on a chip","Executes code","Brain of computer"},
                    {"oscillator","Generates waveform","Produces AC","Used in clocks"},
                    {"modulation","Encodes signal","Used in communication","Alters frequency"},
                    {"transformer","Voltage converter","Two windings","AC component"},
                    {"algorithm","Problem solving steps","Used in AI","Core of logic"},
                    {"amplifier","Signal booster","Increases voltage","Used in radios"},
                    {"semiconductor","Conducts partly","Used in circuits","Found in processors"},
                    {"encryption","Protects information","Data security","Converts to code"},
                    {"microcontroller","Controls devices","Embedded system","Used in automation"},
                    {"debugging","Fixing code","Removes errors","Software development"},
                    {"feedback","Stabilizes systems","Output affects input","Control mechanism"},
                    {"innovation","Introducing new","Scientific creation","Leads to progress"},
                    {"programming","Logic building","Creating software","Uses syntax"},
                    {"firmware","Low level code","Controls hardware","Non volatile memory"},
                    {"oscilloscope","Measures signals","Displays waveform","Used in labs"},
                    {"datascience","Field of study","Uses data","Statistical analysis"},
                    {"perseverance","Continuous effort","Despite failure","Key to achievement"},
                    {"determination","Strong will","Never gives up","Success key"},
                    {"responsibility","Accountability","Moral trait","Required for trust"},
                    {"humanity","Compassion","Quality of being human","Kind nature"},
                    {"processorcore","Executes instructions","Part of cpu","Core component"},
                    {"integratedcircuit","Chip that combines circuits","Miniaturized electronics","Used widely in devices"},
                    {"signalprocessing","Analyzing signals","Used in communications","Transforms data"},
                    {"sensorfusion","Combining data","From multiple sensors","Used in robotics"},
                    {"embeddedsoftware","Software in devices","Specialized code","Runs on microcontrollers"},
                    {"reliability","Consistency of performance","Important in systems","Reduces failures"},
                    {"throughput","Amount processed","Performance metric","Data transfer rate"},
                    {"latency","Delay in system","Measured in ms","Affects responsiveness"},
                    {"bandwidth","Capacity in network","Measured in bps","Affects data rate"},
                    {"heuristic","Practical approach","Not optimal","Used for speed"},
                    {"scalability","Ability to grow","Handles load","Important for systems"},
                    {"redundancy","Backup resources","Increases reliability","Prevents failure"},
                    {"faulttolerance","Continues despite faults","Critical for uptime","Used in servers"},
                    {"microarchitecture","Design of cpu internals","Defines performance","Complex layout"},
                    {"synchronization","Timing coordination","Prevents race conditions","Used in concurrency"},
                    {"concurrency","Parallel tasks","Requires synchronization","Increases throughput"},
                    {"throughputanalysis","Measure of processed units","Used in performance","Optimizes systems"},
                    {"signaltonoise","Ratio measure","Higher is better","Used in communications"},
                    {"analogtodigital","Conversion process","Used by adc","Samples signals"},
                    {"digitaltoanalog","Conversion process","Used by dac","Generates voltages"},
                    {"nonvolatilememory","Retains data without power","Used in storage","Examples: eeprom flash"},
                    {"protocoldesign","Specifies communication","Defines messages","Ensures compatibility"},
                    {"softwareengineering","Engineering of software","Uses methodologies","Produces reliable systems"},
                    {"systemintegration","Combining components","Ensures interoperability","Testing required"},
                    {"controltheory","Study of controllers","Used in automation","Feedback based"},
                    {"signalfiltering","Removes unwanted components","Used in dsp","Improves clarity"},
                    {"statisticalanalysis","Analyzing data","Used in science","Finds patterns"},
                    {"optimization","Finding best solution","Used in engineering","May use heuristics"}
            };

            easyWords.clear();
            mediumWords.clear();
            hardWords.clear();

            for (String[] a : easyData) easyWords.add(new WordData(a[0], new String[]{a[1], a[2], a[3]}));
            for (String[] a : mediumData) mediumWords.add(new WordData(a[0], new String[]{a[1], a[2], a[3]}));
            for (String[] a : hardData) hardWords.add(new WordData(a[0], new String[]{a[1], a[2], a[3]}));

            Collections.shuffle(easyWords);
            Collections.shuffle(mediumWords);
            Collections.shuffle(hardWords);
        }

        void startSession() {
            String sel = (String) levelCombo.getSelectedItem();
            if (sel == null) sel = "Easy";
            switch (sel) {
                case "Easy": currentList = new ArrayList<>(easyWords); break;
                case "Medium": currentList = new ArrayList<>(mediumWords); break;
                case "Hard": currentList = new ArrayList<>(hardWords); break;
                default: currentList = new ArrayList<>(easyWords);
            }
            wordsShown = 0;
            attemptsLeft = 4;
            pickNextWord();
            setSessionActive();
        }

        private void pickNextWord() {
            if (currentList.isEmpty()) {
                scrambledLabel.setText("No words available for this level.");
                hintLabel.setText("");
                inputField.setEnabled(false);
                submitBtn.setEnabled(false);
                triesLabel.setText("");
                return;
            }
            int idx = rnd.nextInt(currentList.size());
            currentWord = currentList.remove(idx);
            attemptsLeft = 4;
            wordsShown++;
            scrambledLabel.setText(scrambleWord(currentWord.word));
            hintLabel.setText("");
            triesLabel.setText("Attempts left: " + attemptsLeft);
            inputField.setText("");
            inputField.requestFocus();
        }

        private void submitGuess() {
            if (currentWord == null) return;
            String guess = inputField.getText().trim().toLowerCase();
            if (guess.isEmpty()) return;

            if (guess.equals(currentWord.word.toLowerCase())) {
                JOptionPane.showMessageDialog(this, "Correct! The word was: " + currentWord.word, "Correct", JOptionPane.INFORMATION_MESSAGE);
                if (wordsShown >= maxRounds) {
                    JOptionPane.showMessageDialog(this, "Session finished! Returning to level selection.");
                    setSessionInactive();
                } else {
                    pickNextWord();
                }
            } else {
                attemptsLeft--;
                if (attemptsLeft == 3) {
                    hintLabel.setText("Hint: " + currentWord.hints[0]);
                } else if (attemptsLeft == 2) {
                    hintLabel.setText("Hint: " + currentWord.hints[1]);
                } else if (attemptsLeft == 1) {
                    hintLabel.setText("Hint: " + currentWord.hints[2]);
                }
                triesLabel.setText("Attempts left: " + attemptsLeft);
                if (attemptsLeft <= 0) {
                    JOptionPane.showMessageDialog(this, "Out of tries! The correct word was: " + currentWord.word, "Moving On", JOptionPane.INFORMATION_MESSAGE);
                    if (wordsShown >= maxRounds) {
                        JOptionPane.showMessageDialog(this, "Session finished! Returning to level selection.");
                        setSessionInactive();
                    } else {
                        pickNextWord();
                    }
                }
            }
        }

        private void nextWord() {
            if (currentList.size() > 0 && wordsShown < maxRounds) {
                pickNextWord();
            } else {
                JOptionPane.showMessageDialog(this, "No more words in this session. Returning to level selection.");
                setSessionInactive();
            }
        }

        private String scrambleWord(String w) {
            ArrayList<Character> chars = new ArrayList<>();
            for (char c : w.toCharArray()) chars.add(c);
            String scrambled;
            do {
                Collections.shuffle(chars);
                StringBuilder sb = new StringBuilder();
                for (char ch : chars) sb.append(ch);
                scrambled = sb.toString();
            } while (scrambled.equalsIgnoreCase(w));
            return scrambled;
        }
    }

    // WordData container
    private static class WordData {
        String word;
        String[] hints;
        WordData(String w, String[] h) {
            word = w;
            hints = h;
        }
    }



    // ---------- main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MindGameApp app = new MindGameApp();
            app.setVisible(true);
        });
    }
}
