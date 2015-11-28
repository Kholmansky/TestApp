import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Room extends JPanel implements ActionListener {

    private Dimension d;

    private Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color roomColor;

    private boolean inGame = false;
    private boolean dying = false;

    private final int blockSize = 30;
    private final int nrOfBlocks = 15;
    private final int scrSize = nrOfBlocks * blockSize;

    private final int maxRobots = 12;
    private final int personSpeed = 10;



    private int nrOfRobots = 10;
    private int pacsleft, score;////////////
    private int[] dx, dy;
    private int[] robotX, robotY, robotDX, robotDY, robotSpeed;

    private Image robot;
    private Image person;


    private int personX, personY, personDX, personDY;
    private int reqdx, reqdy, viewdx, viewdy;//////////////////////

    private final short leveldata[] = {
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };


    private final int validspeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxspeed = 6;

    private int currentspeed = 3;
    private short[] screendata;
    private Timer timer;

    public Room() {

        loadImages();
        initVariables();

        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.BLACK);
        setDoubleBuffered(true);
    }

    private void initVariables() {

        screendata = new short[nrOfBlocks * nrOfBlocks];
        roomColor = new Color(5, 100, 5);
        d = new Dimension(400, 400);
        robotX = new int[maxRobots];
        robotDX = new int[maxRobots];
        robotY = new int[maxRobots];
        robotDY = new int[maxRobots];
        robotSpeed = new int[maxRobots];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }



    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            movePerson();
            drawPerson(g2d);
            moveRobots(g2d);
            checkRoom();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(11, 0, 255));
        g2d.fillRect(50, scrSize / 2 - 30, scrSize - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, scrSize / 2 - 30, scrSize - 100, 50);

        String s = "Press 'S' for start";
        Font small = new Font("Helvetica", Font.BOLD, 15);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (scrSize - metr.stringWidth(s)) / 2, scrSize / 2);
    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, scrSize / 2 + 96, scrSize + 16);

        for (i = 0; i < pacsleft; i++) {
            g.drawImage(person, i * 28 + 8, scrSize + 1, this);
        }
    }

    private void checkRoom() {

        short i = 0;
        boolean finished = true;

        while (i < nrOfBlocks * nrOfBlocks && finished) {

            if ((screendata[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (nrOfRobots < maxRobots) {
                nrOfRobots++;
            }

            if (currentspeed < maxspeed) {
                currentspeed++;
            }

            initLevel();
        }
    }

    private void death() {//murio

        pacsleft--;

        if (pacsleft == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveRobots(Graphics2D g2d) {//mover fantasmas

        short i;
        int pos;
        int count;

        for (i = 0; i < nrOfRobots; i++) {
            if (robotX[i] % blockSize == 0 && robotY[i] % blockSize == 0) {
                pos = robotX[i] / blockSize + nrOfBlocks * (int) (robotY[i] / blockSize);

                count = 0;

                if ((screendata[pos] & 1) == 0 && robotDX[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screendata[pos] & 2) == 0 && robotDY[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screendata[pos] & 4) == 0 && robotDX[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screendata[pos] & 8) == 0 && robotDY[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screendata[pos] & 15) == 15) {
                        robotDX[i] = 0;
                        robotDY[i] = 0;
                    } else {
                        robotDX[i] = -robotDX[i];
                        robotDY[i] = -robotDY[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    robotDX[i] = dx[count];
                    robotDY[i] = dy[count];
                }

            }

            robotX[i] = robotX[i] + (robotDX[i] * robotSpeed[i]);
            robotY[i] = robotY[i] + (robotDY[i] * robotSpeed[i]);
            drawRobot(g2d, robotX[i] + 1, robotY[i] + 1);

            if (personX > (robotX[i] - 12) && personX < (robotX[i] + 12)
                    && personY > (robotY[i] - 12) && personY < (robotY[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawRobot(Graphics2D g2d, int x, int y) {

        g2d.drawImage(robot, x, y, this);
    }

    private void movePerson() {

        int pos;
        short ch;

        if (reqdx == -personDX && reqdy == -personDY) {
            personDX = reqdx;
            personDY = reqdy;
            viewdx = personDX;
            viewdy = personDY;
        }

        if (personX % blockSize == 0 && personY % blockSize == 0) {
            pos = personX / blockSize + nrOfBlocks * (int) (personY / blockSize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short) (ch & 15);
                score++;
            }

            if (reqdx != 0 || reqdy != 0) {
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
                        || (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
                        || (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
                        || (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    personDX = reqdx;
                    personDY = reqdy;
                    viewdx = personDX;
                    viewdy = personDY;
                }
            }

            // Check for standstill
            if ((personDX == -1 && personDY == 0 && (ch & 1) != 0)
                    || (personDX == 1 && personDY == 0 && (ch & 4) != 0)
                    || (personDX == 0 && personDY == -1 && (ch & 2) != 0)
                    || (personDX == 0 && personDY == 1 && (ch & 8) != 0)) {
                personDX = 0;
                personDY = 0;
            }
        }
        personX = personX + personSpeed * personDX;
        personY = personY + personSpeed * personDY;
    }

    private void drawPerson(Graphics2D g2d) {

        g2d.drawImage(person, personX + 1, personY + 1, this);
    }

    private void drawRoom(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < scrSize; y += blockSize) {
            for (x = 0; x < scrSize; x += blockSize) {

                g2d.setColor(roomColor);
                g2d.setStroke(new BasicStroke(2));

                if ((screendata[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + blockSize - 1);
                }

                if ((screendata[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + blockSize - 1, y);
                }

                if ((screendata[i] & 4) != 0) {
                    g2d.drawLine(x + blockSize - 1, y, x + blockSize - 1,
                            y + blockSize - 1);
                }

                if ((screendata[i] & 8) != 0) {
                    g2d.drawLine(x, y + blockSize - 1, x + blockSize - 1,
                            y + blockSize - 1);
                }

                if ((screendata[i] & 16) != 0) {
                    g2d.setColor(Color.red);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }

    private void initGame() {

        pacsleft = 3;
        score = 0;
        initLevel();
        nrOfRobots = 6;
        currentspeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < nrOfBlocks * nrOfBlocks; i++) {
            screendata[i] = leveldata[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        for (i = 0; i < nrOfRobots; i++) {

            robotY[i] = 4 * blockSize;
            robotX[i] = 4 * blockSize;
            robotDY[i] = 0;
            robotDX[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentspeed + 1));

            if (random > currentspeed) {
                random = currentspeed;
            }

            robotSpeed[i] = validspeeds[random];
        }

        personX = 7 * blockSize;
        personY = 11 * blockSize;
        personDX = 0;
        personDY = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }

    private void loadImages()
    {
        robot = new ImageIcon(getClass().getResource("/images/android.png")).getImage();
        person = new ImageIcon(getClass().getResource("/images/hero.png")).getImage();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawRoom(g2d);
        drawScore(g2d);

        if (inGame)
        {
            playGame(g2d);
        } else
        {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter
    {

        @Override
        public void keyPressed(KeyEvent e)
        {

            int key = e.getKeyCode();

            if (inGame)
            {
                if (key == KeyEvent.VK_LEFT)
                {
                    reqdx = -1;
                    reqdy = 0;
                } else if (key == KeyEvent.VK_RIGHT)
                {
                    reqdx = 1;
                    reqdy = 0;
                } else if (key == KeyEvent.VK_UP)
                {
                    reqdx = 0;
                    reqdy = -1;
                } else if (key == KeyEvent.VK_DOWN)
                {
                    reqdx = 0;
                    reqdy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning())
                {
                    inGame = false;
                } else if (key == KeyEvent.VK_PAUSE)
                {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    inGame = true;
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN)
            {
                reqdx = 0;
                reqdy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        repaint();
    }
}