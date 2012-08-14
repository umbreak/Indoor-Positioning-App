package android.ui.twitter.store;


public interface CredentialStore {

  String[] read();
  void write(String[]response);
  void clearCredentials();
}
