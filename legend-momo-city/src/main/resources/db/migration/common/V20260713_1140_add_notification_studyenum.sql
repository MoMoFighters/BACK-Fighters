ALTER TABLE `notification`
    MODIFY COLUMN `type` ENUM(
        'APPROVAL',
        'FRIEND_REQUEST',
        'MESSAGE',
        'GUESTBOOK',
        'POST',
        'CALENDAR',
        'STUDY_INVITE'
    ) NOT NULL;

ALTER TABLE `notification`
    MODIFY COLUMN `is_read` BOOLEAN NULL;