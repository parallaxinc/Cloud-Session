# ---------------------------------------------------
# What does this do for loading configuration files?
# ---------------------------------------------------
# Fake Section Header
#
# fp is a file handle obtained from a call to
# io.open('config_file_path')
#
# ---------------------------------------------------


class FakeSecHead(object):
    def __init__(self, fp):
        self.fp = fp
        self.sec_head = '[section]\n'

    def readline(self):
        if self.sec_head:
            try:
                return self.sec_head
            finally:
                self.sec_head = None
        else:
            return self.fp.readline()
