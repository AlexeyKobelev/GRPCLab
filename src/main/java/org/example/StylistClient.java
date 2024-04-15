package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class StylistClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        org.example.users.StylistServiceGrpc.StylistServiceBlockingStub stub = org.example.users.StylistServiceGrpc.newBlockingStub(channel);

        org.example.users.StylistClient.StylistRequest request = org.example.users.StylistClient.StylistRequest.newBuilder().setStylistId(1).build();
        org.example.users.StylistClient.StylistResponse response = stub.getStylistDetails(request);

        System.out.println("Stylist ID: " + response.getStylist().getId());
        System.out.println("Stylist Name: " + response.getStylist().getName());
        System.out.println("Clients:");
        for (org.example.users.StylistClient.Client client : response.getClientsList()) {
            System.out.println("Client ID: " + client.getId());
            System.out.println("Client Name: " + client.getName());
        }

        channel.shutdown();
    }
}
