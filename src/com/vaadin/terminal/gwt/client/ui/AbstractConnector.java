package com.vaadin.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.communication.ClientRpc;

/* 
 @VaadinApache2LicenseForJavaFiles@
 */

public abstract class AbstractConnector implements Connector {

    private ApplicationConnection connection;
    private String id;

    private Map<String, Collection<ClientRpc>> rpcImplementations;

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.terminal.gwt.client.VPaintable#getConnection()
     */
    public final ApplicationConnection getConnection() {
        return connection;
    }

    private final void setConnection(ApplicationConnection connection) {
        this.connection = connection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.terminal.gwt.client.Connector#getId()
     */
    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * Called once by the framework to initialize the connector.
     * 
     * Custom widgets should not override this method, override init instead;
     * 
     * Note that the shared state is not yet available at this point.
     */
    public final void doInit(String connectorId,
            ApplicationConnection connection) {
        setConnection(connection);
        setId(connectorId);

        init();
    }

    /**
     * Called when the connector has been initialized. Override this method to
     * perform initialization of the connector.
     */
    // FIXME: It might make sense to make this abstract to force users to use
    // init instead of constructor, where connection and id has not yet been
    // set.
    protected void init() {

    }

    /**
     * Registers an implementation for a server to client RPC interface.
     * 
     * Multiple registrations can be made for a single interface, in which case
     * all of them receive corresponding RPC calls.
     * 
     * @param rpcInterface
     *            RPC interface
     * @param implementation
     *            implementation that should receive RPC calls
     */
    protected <T extends ClientRpc> void registerRpc(Class<T> rpcInterface,
            T implementation) {
        String rpcInterfaceId = rpcInterface.getName().replaceAll("\\$", ".");
        if (null == rpcImplementations) {
            rpcImplementations = new HashMap<String, Collection<ClientRpc>>();
        }
        if (null == rpcImplementations.get(rpcInterfaceId)) {
            rpcImplementations.put(rpcInterfaceId, new ArrayList<ClientRpc>());
        }
        rpcImplementations.get(rpcInterfaceId).add(implementation);
    }

    /**
     * Unregisters an implementation for a server to client RPC interface.
     * 
     * @param rpcInterface
     *            RPC interface
     * @param implementation
     *            implementation to unregister
     */
    protected <T extends ClientRpc> void unregisterRpc(Class<T> rpcInterface,
            T implementation) {
        String rpcInterfaceId = rpcInterface.getName().replaceAll("\\$", ".");
        if (null != rpcImplementations
                && null != rpcImplementations.get(rpcInterfaceId)) {
            rpcImplementations.get(rpcInterfaceId).remove(implementation);
        }
    }

    public <T extends ClientRpc> Collection<T> getRpcImplementations(
            String rpcInterfaceId) {
        if (null == rpcImplementations) {
            return Collections.emptyList();
        }
        return (Collection<T>) rpcImplementations.get(rpcInterfaceId);
    }

}