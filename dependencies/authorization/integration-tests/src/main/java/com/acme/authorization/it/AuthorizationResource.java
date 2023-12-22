/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.authorization.it;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/authorization")
@ApplicationScoped
public class AuthorizationResource {
    // add some rest methods here

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String hello() {
        return Json.createObjectBuilder()
                .add("message", "Hello world!")
                .build()
                .toString();
    }

    @RolesAllowed({"VIEW_ALL"})
    @GET
    @Path("user")
    public Uni<Response> user(@Context SecurityContext context) {
        return Uni.createFrom().item(Response.ok(context.getUserPrincipal()).build());
    }

    @GET
    @Path("user_get_data")
    @RolesAllowed({"GET_DATA"})
    public Uni<Response> user1(@Context SecurityContext context) {
        return Uni.createFrom().item(Response.ok(context.getUserPrincipal()).build());
    }
}
