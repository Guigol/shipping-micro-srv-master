package com.example.shippingService.config;

import com.example.shippingService.dtos.ContactInfo;
import com.example.shippingService.dtos.ShipmentRequest;
import com.example.shippingService.entities.Shipment;
import com.example.shippingService.repositories.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"docker", "local"})
public class ShipmentDataLoader implements CommandLineRunner {

    private final ShipmentRepository shipmentRepository;
    private final Random random = new Random();

    public static double generateRandomWeight() {
        return BigDecimal
                .valueOf(ThreadLocalRandom.current().nextDouble(0.0, 8.0))
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public void run(String... args) {

        if (shipmentRepository.count() > 0) {
            log.info("Shipments already exist, skipping data load");
            return;
        }

        List<ShipmentRequest> shipmentRequests = List.of(

                buildShipment(
                        "LP-100001-F", "CREATED", "LA POSTE",
                        "Charlie Tronchon", "105 rue Introuvable - Montrouge",
                        "Paul Lux", "9 allée des Fleurs - Honfleur"
                ),

                buildShipment(
                        "LP-100002-F", "IN_TRANSIT", "DHL",
                        "Mona Ramone", "8 rue de la Grande Truanderie - 75001 Paris",
                        "Julien Torres", "5 place Rouge - Cagnes-sur-Mer"
                ),

                buildShipment(
                        "LP-100003-F", "DELIVERED", "COLISSIMO",
                        "Société DULECO", "3 impasse des Oiseaux - Montreuil",
                        "Rachid Taha", "Chemin des Vignes - Mongé-en-Gohelle"
                ),

                buildShipment(
                        "LP-100004-F", "CREATED", "COLISSIMO",
                        "Société DULECO", "3 impasse des Oiseaux - Montreuil",
                        "Martin Lecointre", "20 rue des Myrtilles - Marseille"
                ),

                buildShipment(
                        "LP-100005-F", "IN_TRANSIT", "COLISSIMO",
                        "Société DULECO", "3 impasse des Oiseaux - Montreuil",
                        "Valérie Larbeau", "1 quai de Seine - Valence"
                ),

                buildShipment(
                        "LP-100006-F", "CREATED", "COLISSIMO",
                        "Société DULECO", "3 impasse des Oiseaux - Montreuil",
                        "Napoléon Premier", "8 rue de l'Élysée - Narbonne"
                ),

                buildShipment(
                        "LP-100007-F", "DELIVERED", "MONDIAL RELAY",
                        "Jacqueline Merlot", "108 avenue Le Prince - Le Tréport",
                        "Paul Lux", "9 allée des Fleurs - Honfleur"
                ),

                buildShipment(
                        "LP-100008-F", "IN_TRANSIT", "DHL",
                        "Mr. Bean", "286 avenue de Verdun - Châtillon",
                        "Violette Pagan", "40 boulevard Massena - Rennes"
                ),

                buildShipment(
                        "LP-100009-F", "CREATED", "LA POSTE",
                        "Georges Noix", "59 rue Filibert - 45216 Le Treuille",
                        "Robert Patin", "25 place Lefort - Mazarin"
                ),

                buildShipment(
                        "LP-100010-F", "DELIVERED", "DHL",
                        "Serge Blanc", "17 rue Monsoleil - Le Croisic",
                        "Gaston Lebon", "7 avenue Émile Dugain - La Baule"
                ),

                buildShipment(
                        "LP-100011-F", "IN_TRANSIT", "COLISSIMO",
                        "Société DULECO", "3 impasse des Oiseaux - Montreuil",
                        "Yvette Goudy", "52 rue de Vernois - Montpellier"
                ),

                buildShipment(
                        "LP-100012-F", "CREATED", "MONDIAL RELAY",
                        "Berthold Brecht", "2 allée du Hameau Fertile - Gentenay",
                        "Maria Ross", "13 boulevard Lefort - Saint-Quentin"
                ),

                buildShipment(
                        "LP-100013-F", "IN_TRANSIT", "DHL",
                        "Eric Badin", "28 rue de Vernois - Montrouge",
                        "Jacqueline Dormont", "Résidence Les Roses, 8 rue du Canal - Bordeaux"
                )
        );

        List<Shipment> shipmentEntities = shipmentRequests.stream()
                .map(this::toEntity)
                .toList();

        shipmentRepository.saveAll(shipmentEntities);
        log.info("✅ Loaded {} sample shipments", shipmentEntities.size());
    }

    private ShipmentRequest buildShipment(
            String trackingNumber,
            String status,
            String carrier,
            String senderName,
            String senderAddress,
            String receiverName,
            String receiverAddress
    ) {

        ShipmentRequest shipment = new ShipmentRequest();
        shipment.setShipmentId("SHIP-" + UUID.randomUUID().toString().substring(0, 16));
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCurrentStatus(status);
        shipment.setCarrier(carrier);
        shipment.setWeight(generateRandomWeight());
        shipment.setCreatedAt(Instant.now().toString());
        shipment.setUpdatedAt(Instant.now().toString());

        shipment.setSender(new ContactInfo(senderName, senderAddress));
        shipment.setReceiver(new ContactInfo(receiverName, receiverAddress));

        shipment.setStatusHistory(List.of());
        shipment.setFiles(List.of());

        return shipment;
    }

    @SuppressWarnings("unchecked")
    private Shipment toEntity(ShipmentRequest request) {

        Shipment shipment = new Shipment();
        shipment.setShipmentId(request.getShipmentId());
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setCurrentStatus(request.getCurrentStatus());
        shipment.setCarrier(request.getCarrier());
        shipment.setWeight_kg(request.getWeight());

        // 🔹 USER ID entre 1 et 8
        shipment.setUserId(random.nextInt(8) + 1);

        shipment.setSender(request.getSender());
        shipment.setReceiver(request.getReceiver());

        if (request.getStatusHistory() instanceof List<?> list) {
            shipment.setStatusHistory((List<Map<String, Object>>) list);
        } else {
            shipment.setStatusHistory(new ArrayList<>());
        }

        if (request.getFiles() instanceof Map<?, ?> map) {
            shipment.setFiles((Map<String, Map<String, Object>>) map);
        } else {
            shipment.setFiles(new HashMap<>());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("weight_kg", request.getWeight());
        shipment.setMetadata(metadata);

        shipment.setCreatedAt(
                request.getCreatedAt() != null
                        ? Instant.parse(request.getCreatedAt())
                        : Instant.now()
        );
        shipment.setUpdatedAt(
                request.getUpdatedAt() != null
                        ? Instant.parse(request.getUpdatedAt())
                        : Instant.now()
        );

        return shipment;
    }
}
