import java.awt.EventQueue;
import javax.swing.JFrame;

public class Game extends JFrame {

    public Game() {

        initUI();
    }

    private void initUI() {

        add(new Room());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(470, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                Game ex = new Game();
                ex.setVisible(true);
            }
        });
    }
}