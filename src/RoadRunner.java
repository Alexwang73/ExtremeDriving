public class RoadRunner {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new RoadFrame();
            new MusicPlayerGUI("C:\\Users\\YourName\\Music\\your_song.wav");
        });
    }
}