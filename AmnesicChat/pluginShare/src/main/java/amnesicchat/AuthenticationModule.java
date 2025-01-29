public interface AuthenticationModule {
    // Module Name
    String getName();

    // Get author of the module
    String getAuthor();

    // Main method to execute the module
    void run();
}
