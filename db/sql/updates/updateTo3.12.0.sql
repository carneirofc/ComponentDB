--
-- Copyright (c) UChicago Argonne, LLC. All rights reserved.
-- See LICENSE file.
--

-- Execute by running `mysql CDB_DB_NAME --host=localhost --user=cdb -p < updateTo3.12.0.sql`


insert into user_group (name, description) value ('CDB_MAINTAINER', 'System Maintainer Group');
insert into user_group (name, description) value ('CDB_ADVANCED', 'System Advanced Group');
