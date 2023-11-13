package bear;

import java.io.IOException;
import java.io.InputStream;

interface InputStreamSupplier {

    InputStream get() throws IOException;
}
