public class RoadRunner {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new RoadFrame();

            new MusicPlayerGUI("src/Instrumental.wav");
        });
    }
}