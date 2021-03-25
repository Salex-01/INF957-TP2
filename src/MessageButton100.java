import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButton100 extends MessageButton {
    public MessageButton100(Container c1, Main4 m) {
        super(c1, m, 4);
        setLabel("100 aléatoires");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        sendMessages(100, -1, -1);
    }
}