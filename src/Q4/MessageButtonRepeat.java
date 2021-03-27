package Q4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButtonRepeat extends GraphButton {
    public MessageButtonRepeat(Container c1, Main4 m) {
        super(c1, m, 1);
        setLabel("Renvoyer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!m4.g.repeatLast()){
            JOptionPane.showMessageDialog(null,"Aucun message n'a encore été envoyé");
        }
    }
}