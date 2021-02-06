package com.contrastsecurity.comware.json;

import java.util.List;

import com.contrastsecurity.comware.model.Route;

public class RouteJson extends ContrastJson {
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

}
