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
