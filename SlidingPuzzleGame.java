import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SlidingPuzzleGame extends JFrame {
    private static final int BOARD_SIZE = 600;
    private static final int DEFAULT_GRID_SIZE = 3;
    private static final Random RANDOM = new Random();

    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton size3Button = new JButton("\u4e5d\u5bab\u683c");
    private final JButton size4Button = new JButton("\u5341\u516d\u5bab\u683c");
    private final JButton size5Button = new JButton("\u4e8c\u5341\u4e94\u5bab\u683c");
    private final JButton chooseImageButton = new JButton("\u9009\u62e9\u7167\u7247");
    private final JButton shuffleButton = new JButton("\u91cd\u65b0\u6253\u4e71");
    private final JButton previewButton = new JButton("\u67e5\u770b\u539f\u56fe");
    private final Timer timer;

    private BufferedImage sourceImage;
    private BufferedImage fittedImage;
    private int gridSize = DEFAULT_GRID_SIZE;
    private int[][] board;
    private int emptyRow;
    private int emptyCol;
    private int moveCount;
    private int elapsedSeconds;
    private boolean solved;

    public SlidingPuzzleGame() {
        super("\u6ed1\u52a8\u7167\u7247\u62fc\u56fe");
        sourceImage = createDefaultImage(900, 900);

        timer = new Timer(1000, event -> {
            if (!solved) {
                elapsedSeconds++;
                updateStatus();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(createToolbar(), BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        bindActions();
        setupKeyboardControls();
        startNewGame(DEFAULT_GRID_SIZE);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(680, 760));
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new GridLayout(1, 6, 8, 0));
        toolbar.add(size3Button);
        toolbar.add(size4Button);
        toolbar.add(size5Button);
        toolbar.add(chooseImageButton);
        toolbar.add(shuffleButton);
        toolbar.add(previewButton);
        return toolbar;
    }

    private void bindActions() {
        size3Button.addActionListener(event -> startNewGame(3));
        size4Button.addActionListener(event -> startNewGame(4));
        size5Button.addActionListener(event -> startNewGame(5));
        chooseImageButton.addActionListener(this::chooseImage);
        shuffleButton.addActionListener(event -> startNewGame(gridSize));
        previewButton.addActionListener(event -> showPreview());
    }

    private void setupKeyboardControls() {
        boardPanel.setFocusable(true);
        boardPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        moveTile(emptyRow + 1, emptyCol);
                        break;
                    case KeyEvent.VK_DOWN:
                        moveTile(emptyRow - 1, emptyCol);
                        break;
                    case KeyEvent.VK_LEFT:
                        moveTile(emptyRow, emptyCol + 1);
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveTile(emptyRow, emptyCol - 1);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void chooseImage(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("\u56fe\u7247\u6587\u4ef6", "jpg", "jpeg", "png", "bmp", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new IOException("\u65e0\u6cd5\u8bc6\u522b\u56fe\u7247\u683c\u5f0f");
            }
            sourceImage = image;
            startNewGame(gridSize);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "\u8bfb\u53d6\u56fe\u7247\u5931\u8d25\uff1a" + ex.getMessage(), "\u9519\u8bef", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPreview() {
        JLabel preview = new JLabel();
        preview.setIcon(new javax.swing.ImageIcon(fittedImage));
        JOptionPane.showMessageDialog(this, preview, "\u5b8c\u6574\u7167\u7247", JOptionPane.PLAIN_MESSAGE);
        boardPanel.requestFocusInWindow();
    }

    private void startNewGame(int size) {
        gridSize = size;
        fittedImage = fitToSquare(sourceImage, BOARD_SIZE);
        board = new int[gridSize][gridSize];
        int value = 1;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                board[row][col] = value++;
            }
        }

        emptyRow = gridSize - 1;
        emptyCol = gridSize - 1;
        board[emptyRow][emptyCol] = 0;
        moveCount = 0;
        elapsedSeconds = 0;
        solved = false;

        shuffleByLegalMoves();
        timer.restart();
        updateStatus();
        boardPanel.repaint();
        boardPanel.requestFocusInWindow();
    }

    private void shuffleByLegalMoves() {
        int shuffleMoves = gridSize * gridSize * 80;
        int previousRow = -1;
        int previousCol = -1;

        for (int i = 0; i < shuffleMoves; i++) {
            List<int[]> candidates = getMovableTiles();
            for (int index = candidates.size() - 1; index >= 0; index--) {
                int[] tile = candidates.get(index);
                if (tile[0] == previousRow && tile[1] == previousCol) {
                    candidates.remove(index);
                }
            }
            if (candidates.isEmpty()) {
                candidates = getMovableTiles();
            }

            int oldEmptyRow = emptyRow;
            int oldEmptyCol = emptyCol;
            int[] tile = candidates.get(RANDOM.nextInt(candidates.size()));
            swapWithEmpty(tile[0], tile[1]);
            previousRow = oldEmptyRow;
            previousCol = oldEmptyCol;
        }

        if (isSolved()) {
            List<int[]> candidates = getMovableTiles();
            Collections.shuffle(candidates, RANDOM);
            int[] tile = candidates.get(0);
            swapWithEmpty(tile[0], tile[1]);
        }
    }

    private List<int[]> getMovableTiles() {
        List<int[]> tiles = new ArrayList<>();
        addTileIfInside(tiles, emptyRow - 1, emptyCol);
        addTileIfInside(tiles, emptyRow + 1, emptyCol);
        addTileIfInside(tiles, emptyRow, emptyCol - 1);
        addTileIfInside(tiles, emptyRow, emptyCol + 1);
        return tiles;
    }

    private void addTileIfInside(List<int[]> tiles, int row, int col) {
        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            tiles.add(new int[]{row, col});
        }
    }

    private void moveTile(int row, int col) {
        if (solved || !isAdjacentToEmpty(row, col)) {
            return;
        }

        swapWithEmpty(row, col);
        moveCount++;
        solved = isSolved();
        if (solved) {
            timer.stop();
        }
        updateStatus();
        boardPanel.repaint();
    }

    private boolean isAdjacentToEmpty(int row, int col) {
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
            return false;
        }
        return Math.abs(row - emptyRow) + Math.abs(col - emptyCol) == 1;
    }

    private void swapWithEmpty(int row, int col) {
        board[emptyRow][emptyCol] = board[row][col];
        board[row][col] = 0;
        emptyRow = row;
        emptyCol = col;
    }

    private boolean isSolved() {
        int expected = 1;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (row == gridSize - 1 && col == gridSize - 1) {
                    return board[row][col] == 0;
                }
                if (board[row][col] != expected++) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateStatus() {
        String status = gridSize + " x " + gridSize
                + " | \u6b65\u6570\uff1a" + moveCount
                + " | \u7528\u65f6\uff1a" + formatTime(elapsedSeconds);
        if (solved) {
            status += " | \u5b8c\u6210\uff01";
        } else {
            status += " | \u70b9\u51fb\u7a7a\u4f4d\u65c1\u8fb9\u7684\u62fc\u56fe\uff0c\u6216\u7528\u65b9\u5411\u952e\u79fb\u52a8\u3002";
        }
        statusLabel.setText(status);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int restSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, restSeconds);
    }

    private BufferedImage fitToSquare(BufferedImage image, int size) {
        int side = Math.min(image.getWidth(), image.getHeight());
        int x = (image.getWidth() - side) / 2;
        int y = (image.getHeight() - side) / 2;

        BufferedImage square = image.getSubimage(x, y, side, side);
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(square, 0, 0, size, size, null);
        graphics.dispose();
        return scaled;
    }

    private BufferedImage createDefaultImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int y = 0; y < height; y++) {
            float ratio = (float) y / height;
            graphics.setColor(new Color(40 + (int) (ratio * 40), 112 + (int) (ratio * 70), 180 + (int) (ratio * 40)));
            graphics.drawLine(0, y, width, y);
        }

        graphics.setColor(new Color(255, 214, 102));
        graphics.fillOval(620, 80, 150, 150);
        graphics.setColor(new Color(255, 255, 255, 210));
        graphics.fillRoundRect(130, 130, 300, 90, 45, 45);
        graphics.fillRoundRect(310, 95, 260, 80, 40, 40);

        graphics.setColor(new Color(54, 150, 89));
        graphics.fillOval(-120, 520, 580, 360);
        graphics.setColor(new Color(40, 120, 70));
        graphics.fillOval(350, 500, 680, 420);

        graphics.setColor(new Color(225, 72, 79));
        graphics.fillRoundRect(255, 330, 390, 260, 32, 32);
        graphics.setColor(new Color(255, 244, 214));
        graphics.fillRoundRect(325, 390, 250, 145, 24, 24);
        graphics.setColor(new Color(80, 55, 50));
        graphics.setStroke(new BasicStroke(14));
        graphics.drawLine(255, 335, 450, 205);
        graphics.drawLine(645, 335, 450, 205);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 64));
        graphics.setColor(new Color(32, 48, 64));
        drawCenteredString(graphics, "PHOTO PUZZLE", width, 700);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        drawCenteredString(graphics, "3 x 3   4 x 4   5 x 5", width, 755);
        graphics.dispose();
        return image;
    }

    private void drawCenteredString(Graphics2D graphics, String text, int width, int y) {
        FontMetrics metrics = graphics.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        graphics.drawString(text, x, y);
    }

    private final class BoardPanel extends JPanel {
        private BoardPanel() {
            setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
            setBackground(new Color(28, 33, 40));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent event) {
                    int tileSize = getTileSize();
                    int col = event.getX() / tileSize;
                    int row = event.getY() / tileSize;
                    moveTile(row, col);
                    requestFocusInWindow();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int tileSize = getTileSize();
            int offsetX = (getWidth() - tileSize * gridSize) / 2;
            int offsetY = (getHeight() - tileSize * gridSize) / 2;

            graphics2D.setColor(new Color(18, 22, 28));
            graphics2D.fillRoundRect(offsetX - 4, offsetY - 4, tileSize * gridSize + 8, tileSize * gridSize + 8, 14, 14);

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int value = board[row][col];
                    int x = offsetX + col * tileSize;
                    int y = offsetY + row * tileSize;
                    if (value == 0) {
                        paintEmptyTile(graphics2D, x, y, tileSize);
                    } else {
                        paintImageTile(graphics2D, value, x, y, tileSize);
                    }
                }
            }
            graphics2D.dispose();
        }

        private int getTileSize() {
            return Math.min(getWidth(), getHeight()) / gridSize;
        }

        private void paintImageTile(Graphics2D graphics, int value, int x, int y, int tileSize) {
            int originalIndex = value - 1;
            int sourceRow = originalIndex / gridSize;
            int sourceCol = originalIndex % gridSize;
            int sourceTileSize = fittedImage.getWidth() / gridSize;

            graphics.drawImage(
                    fittedImage,
                    x, y, x + tileSize, y + tileSize,
                    sourceCol * sourceTileSize,
                    sourceRow * sourceTileSize,
                    (sourceCol + 1) * sourceTileSize,
                    (sourceRow + 1) * sourceTileSize,
                    null
            );

            graphics.setColor(new Color(255, 255, 255, 180));
            graphics.setStroke(new BasicStroke(2));
            graphics.drawRect(x, y, tileSize, tileSize);

            graphics.setColor(new Color(0, 0, 0, 120));
            graphics.fillRoundRect(x + 8, y + 8, 42, 28, 10, 10);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            graphics.drawString(String.valueOf(value), x + 20, y + 28);
        }

        private void paintEmptyTile(Graphics2D graphics, int x, int y, int tileSize) {
            graphics.setColor(new Color(18, 22, 28));
            graphics.fillRect(x, y, tileSize, tileSize);
            graphics.setColor(new Color(255, 255, 255, 70));
            graphics.setStroke(new BasicStroke(2));
            graphics.drawRect(x + 3, y + 3, tileSize - 6, tileSize - 6);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SlidingPuzzleGame().setVisible(true));
    }
}
