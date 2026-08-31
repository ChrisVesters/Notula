/**
 * Identifies this client for the lifetime of the page, so the events caused by
 * its own actions can be recognised when the server echoes them back.
 *
 * Deliberately not stored: it must not be shared between tabs, or a tab would
 * drop the events of the other. It also has to survive a reconnect, which it
 * does because the constant outlives the WebSocketClient that a session
 * refresh replaces.
 */
const CLIENT_ID: string = crypto.randomUUID();

export default CLIENT_ID;
