 --先创建数据库
  -- cd /opt/db
  -- sudo sqlite3 book.db
  -- chmod 777 /opt/db
  -- chmod 777 book.db
    --drop table book;
    CREATE TABLE book (
                id integer PRIMARY KEY autoincrement,
                role_name TEXT,
                    story_desc TEXT,
                    create_time default (datetime('now', 'localtime')),
                    video_url TEXT,
                    error TEXT,
                    batch_id TEXT,
                    user_id TEXT,
                    story_system_message TEXT,
                    story_user_message TEXT,
                    story_output_message TEXT,
                    status TEXT
            );