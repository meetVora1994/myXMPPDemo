package org.apache.android.xmpp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.sql.Connection;
import java.util.ArrayList;

public class XMPPClient extends Activity {

    ArrayAdapter<String> adapter;
    private ArrayList<String> messages = new ArrayList();
    private Handler mHandler = new Handler();
    private SettingsDialog mDialog;
    private RegistrationDialog mRegistrationDialog;
    private EditText mRecipient;
    private EditText mSendText;
    private ListView mList;
    private XMPPConnection connection;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("myTAG", "onCreate called");
        setContentView(R.layout.main);

        mRecipient = (EditText) this.findViewById(R.id.recipient);
        Log.i("myTAG", "mRecipient = " + mRecipient);
        mSendText = (EditText) this.findViewById(R.id.sendText);
        Log.i("myTAG", "mSendText = " + mSendText);
        mList = (ListView) this.findViewById(R.id.listMessages);
        Log.i("myTAG", "mList = " + mList);

        adapter = new ArrayAdapter<>(this, R.layout.multi_line_list_item, messages);
        mList.setAdapter(adapter);

        // Dialog for getting the xmpp settings
        mDialog = new SettingsDialog(this);

        // Dialog for registering new user on server
        mRegistrationDialog = new RegistrationDialog(this);

        // Set a listener to show the settings dialog
        Button setup = (Button) this.findViewById(R.id.setup);
        setup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mDialog.show();
                    }
                });
            }
        });

        Button register = (Button) this.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mRegistrationDialog.show();
                    }
                });
            }
        });

        // Set a listener to send a chat text message
        Button send = (Button) this.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    String to = mRecipient.getText().toString();
                    String text = mSendText.getText().toString();

                    Log.i("myTAG", "Sending text [" + text + "] to [" + to + "]");
                    Message msg = new Message(to, Message.Type.chat);
                    msg.setBody(text);
                    connection.sendPacket(msg);
                    messages.add(connection.getUser() + ":");
                    messages.add(text);
                    adapter.notifyDataSetChanged();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Called by Settings dialog when a connection is establised with the XMPP server
     *
     * @param connection
     */
    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
        if (connection != null) {
            // Add a packet listener to get messages sent to us
            StanzaFilter filter = MessageTypeFilter.CHAT;
            connection.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    Message message = (Message) packet;
                    if (message.getBody() != null) {
                        String fromName = message.getFrom();
                        Log.i("myTAG", "Got text [" + message.getBody() + "] from [" + fromName + "]");
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        // Add the incoming message to the list view
                        mHandler.post(new Runnable() {
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                }
            }, filter);

//            //Setting Chat room invitation Listener
//            MultiUserChat muc = new MultiUserChat(connection, "", null);
//            MultiUserChat.addInvitationListener(mXmppConnection,
//                    new InvitationListener() {
//
//                        @Override
//                        public void invitationReceived(Connection connection,
//                                                       String room, String inviter, String reason,
//                                                       String unKnown, Message message) {
//
//                            //MultiUserChat.decline(mXmppConnection, room, inviter,
//                            //  "Don't bother me right now");
//                            // MultiUserChat.decline(mXmppConnection, room, inviter,
//                            // "Don't bother me right now");
//                            try {
//                                muc.join("test-nick-name");
//                                Log.e("abc", "join room successfully");
//                                muc.sendMessage("I joined this room!! Bravo!!");
//                            } catch (XMPPException e) {
//                                e.printStackTrace();
//                                Log.e("abc", "join room failed!");
//                            }
//                        }
//                    });
        }
    }
}
