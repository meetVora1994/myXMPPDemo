package org.apache.android.xmpp;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class SettingsDialog extends Dialog implements android.view.View.OnClickListener {
    private XMPPClient xmppClient;

    public SettingsDialog(XMPPClient xmppClient) {
        super(xmppClient);
        this.xmppClient = xmppClient;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.settings);
        getWindow().setFlags(4, 4);
        setTitle("XMPP Settings");
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }

    public void onClick(View v) {
        String host = getText(R.id.host);
        String port = getText(R.id.port);
        String service = getText(R.id.service);
        String username = getText(R.id.userid);
        String password = getText(R.id.password);

        // Create a connection
        XMPPTCPConnectionConfiguration connConfig =
                XMPPTCPConnectionConfiguration.builder()
                .setHost(host)
                .setUsernameAndPassword(username, password)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setPort(Integer.parseInt(port))
                .setDebuggerEnabled(true)
                .setServiceName(service)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(connConfig);

        try {
            connection.connect();
            Log.i("myTag", "[SettingsDialog] Connected to " + connection.getHost());
            connection.login();
        } catch (SmackException | IOException | XMPPException e) {
            Log.e("myTag", "[SettingsDialog] Failed to connect to " + connection.getHost());
            Log.e("myTag", e.getMessage());
            xmppClient.setConnection(null);
        }
        try {
            Log.i("myTag", "Logged in as " + connection.getUser());

            // Set the status to available
            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);
            xmppClient.setConnection(connection);
        } catch (SmackException e) {
            e.printStackTrace();
            Log.e("myTag", e.getMessage());
        }
        dismiss();
    }

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}
