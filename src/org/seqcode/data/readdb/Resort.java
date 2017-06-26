package org.seqcode.data.readdb;

/* this was used to fix a bug in the readdb server.  You shouldn't need it. */

public class Resort {

    public static void main(String args[]) throws Exception {
        Client client = new Client(true);
        for (int i = 0; i < args.length; i++) {
            String alignid = args[i];
            System.err.println("Resorting alignment " + alignid);
            try {
                for (Integer chromid : client.getChroms(alignid,false, false,false)) {
                    client.checksort(alignid,chromid);
                }
            } catch (ClientException e) {
                System.err.println("Couldn't do " + alignid + ": " + e.toString());
            }

        }
        client.close();
    }
}