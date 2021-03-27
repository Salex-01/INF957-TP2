import java.awt.*;
import java.awt.event.ActionEvent;

public class LeaderboardButton extends GraphButton {
    public LeaderboardButton(Container c1, Main4 m) {
        super(c1, m, 8);
        setLabel("Leaderboard");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        m4.g.showLeaderboard();
    }
}