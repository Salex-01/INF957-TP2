import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButton1 extends MessageButton {
    public MessageButton1(Container c1, Main4 m) {
        super(c1, m, 2);
        setLabel("1 aléatoire");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        sendMessages(1, -1, -1);
    }
}
