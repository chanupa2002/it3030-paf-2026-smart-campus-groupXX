package com.uninode.smartcampus.modules.booking.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PendingBookingExpiryNotifier {

    private final JdbcTemplate jdbcTemplate;
    private final BookingSlotService bookingSlotService;

    public PendingBookingExpiryNotifier(JdbcTemplate jdbcTemplate, BookingSlotService bookingSlotService) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookingSlotService = bookingSlotService;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void notifyExpiredPendingBookings() {
        List<PendingExpiryRow> rows = jdbcTemplate.query(
                """
                        SELECT
                            COALESCE(rb.booking_group_id, rb.booking_id) AS booking_group_id,
                            rb.user_id,
                            rb.created_at,
                            rb.date,
                            rb.resource_id,
                            r.name AS resource_name,
                            ds.slot
                        FROM "Resource_booking" rb
                        INNER JOIN "Ds_slot" ds ON ds.slot_id = rb.timeslot_id
                        LEFT JOIN "Resource" r ON r.id = rb.resource_id
                        WHERE LOWER(TRIM(COALESCE(rb.status, ''))) = 'pending'
                        ORDER BY COALESCE(rb.booking_group_id, rb.booking_id), rb.booking_id
                        """,
                (rs, rowNum) -> new PendingExpiryRow(
                        rs.getLong("booking_group_id"),
                        rs.getLong("user_id"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("date", LocalDate.class),
                        (Long) rs.getObject("resource_id"),
                        rs.getString("resource_name"),
                        (Long) rs.getObject("slot")));

        Map<Long, PendingExpiryAccumulator> grouped = new LinkedHashMap<>();
        for (PendingExpiryRow row : rows) {
            grouped.computeIfAbsent(
                    row.bookingGroupId(),
                    ignored -> new PendingExpiryAccumulator(
                            row.bookingGroupId(),
                            row.userId(),
                            row.createdAt(),
                            row.bookingDate(),
                            row.resourceId(),
                            row.resourceName()))
                    .addSlot(row.slot());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (PendingExpiryAccumulator booking : grouped.values()) {
            if (booking.createdAt() == null || booking.userId() == null) {
                continue;
            }

            if (!now.isAfter(booking.createdAt().plusHours(72))) {
                continue;
            }

            bookingSlotService.expirePendingBookingGroup(booking.bookingGroupId(), booking.userId());
        }
    }

    private record PendingExpiryRow(
            Long bookingGroupId,
            Long userId,
            OffsetDateTime createdAt,
            LocalDate bookingDate,
            Long resourceId,
            String resourceName,
            Long slot) {
    }

    private static final class PendingExpiryAccumulator {
        private final Long bookingGroupId;
        private final Long userId;
        private final OffsetDateTime createdAt;
        private final LocalDate bookingDate;
        private final Long resourceId;
        private final String resourceName;
        private final List<Long> slots = new ArrayList<>();

        private PendingExpiryAccumulator(
                Long bookingGroupId,
                Long userId,
                OffsetDateTime createdAt,
                LocalDate bookingDate,
                Long resourceId,
                String resourceName) {
            this.bookingGroupId = bookingGroupId;
            this.userId = userId;
            this.createdAt = createdAt;
            this.bookingDate = bookingDate;
            this.resourceId = resourceId;
            this.resourceName = resourceName;
        }

        private PendingExpiryAccumulator addSlot(Long slot) {
            if (slot != null && !slots.contains(slot)) {
                slots.add(slot);
            }
            return this;
        }

        private Long bookingGroupId() {
            return bookingGroupId;
        }

        private Long userId() {
            return userId;
        }

        private OffsetDateTime createdAt() {
            return createdAt;
        }

        private LocalDate bookingDate() {
            return bookingDate;
        }

    }
}
