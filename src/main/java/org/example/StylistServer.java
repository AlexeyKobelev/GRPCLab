package org.example;

import com.mysql.cj.xdevapi.Client;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.users.StylistClient;

import java.sql.*;

public class StylistServer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/grpc";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1111";

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8080)
                .addService(new StylistServiceImpl())
                .build();

        server.start();
        System.out.println("Server started");
        server.awaitTermination();
    }

    static class StylistServiceImpl extends org.example.users.StylistServiceGrpc.StylistServiceImplBase {
        @Override
        public void getStylistDetails(StylistClient.StylistRequest request, StreamObserver<StylistClient.StylistResponse> responseObserver) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                String sql = "SELECT * FROM stylist WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, request.getStylistId());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int id = resultSet.getInt("id");
                            String name = resultSet.getString("name");
                            StylistClient.Stylist stylist = StylistClient.Stylist.newBuilder().setId(id).setName(name).build();

                            // Получаем клиентов стилиста
                            sql = "SELECT * FROM client WHERE stylistId = ?";
                            try (PreparedStatement clientStatement = connection.prepareStatement(sql)) {
                                clientStatement.setInt(1, id);
                                try (ResultSet clientResultSet = clientStatement.executeQuery()) {
                                    StylistClient.StylistResponse.Builder responseBuilder = StylistClient.StylistResponse.newBuilder().setStylist(stylist);
                                    while (clientResultSet.next()) {
                                        int clientId = clientResultSet.getInt("id");
                                        String clientName = clientResultSet.getString("name");
                                        Client client = Client.newBuilder().setId(clientId).setName(clientName).setId(id).build();
                                        responseBuilder.addClients(client);
                                    }
                                    StylistClient.StylistResponse response = responseBuilder.build();
                                    responseObserver.onNext(response);
                                    responseObserver.onCompleted();
                                }
                            }
                        } else {
                            responseObserver.onError(Status.NOT_FOUND.withDescription("Stylist not found").asRuntimeException());
                        }
                    }
                }
            } catch (SQLException e) {
                responseObserver.onError(Status.INTERNAL.withDescription("Database error").asRuntimeException());
            }
        }
    }
}
