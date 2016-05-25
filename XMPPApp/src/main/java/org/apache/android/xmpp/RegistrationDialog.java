package org.apache.android.xmpp;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class RegistrationDialog extends Dialog implements View.OnClickListener {
    private XMPPClient xmppClient;

    public RegistrationDialog(XMPPClient xmppClient) {
        super(xmppClient);
        this.xmppClient = xmppClient;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.dialog_registration);
        getWindow().setFlags(4, 4);
        setTitle("XMPP User Registration");
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
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setPort(Integer.parseInt(port))
                        .setDebuggerEnabled(true)
                        .setServiceName(service)
                        .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(connConfig);

        try {
            connection.connect();
            Log.i("myTAG", "[SettingsDialog] Connected to " + connection.getHost());

            // Registering the user
            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            if(accountManager.supportsAccountCreation())
                accountManager.createAccount(username, password);
            else
                Toast.makeText(xmppClient, "Sorry, You don't have rights to create a new User account.\nContact server administrator!", Toast.LENGTH_SHORT).show();
        } catch (SmackException | IOException | XMPPException e) {
            Log.e("myTAG", "[RegistrationDialog] Failed to connect to " + connection.getHost());
            Log.e("myTAG", e.getMessage());
            xmppClient.setConnection(null);
        }
//        try {
//            Log.i("myTAG", "Logged in as " + connection.getUser());
//
//            // Set the status to available
//            Presence presence = new Presence(Presence.Type.available);
//            connection.sendPacket(presence);
//            xmppClient.setConnection(connection);
//        } catch (SmackException e) {
//            e.printStackTrace();
//            Log.e("myTAG", e.getMessage());
//        }
        dismiss();
    }

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}
