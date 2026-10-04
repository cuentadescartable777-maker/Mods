import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 * Genera TODOS los assets repetitivos del mod (texturas PNG, blockstates, modelos,
 * recetas, loot tables y archivos de idioma) a partir de la lista de flags() de abajo.
 *
 * Uso (lo ejecuta Gradle automaticamente):  java tools/AssetGenerator.java <carpeta-salida>
 *
 * Para agregar una bandera: agregar una entrada en flags(), escribir su metodo pintor
 * y agregar el mismo id en ModBlocks.FLAG_IDS (Java).
 */
public class AssetGenerator {

    static final String MOD = "argentinaflags";

    /** Lienzo cuadrado; la bandera ocupa 112x72 px (proporcion 14:9) arriba a la izquierda. */
    static final int SIZE = 128, FW = 112, FH = 72;

    // ---- Paleta ----
    static final int CELESTE = 0xFF74ACDF, WHITE = 0xFFFFFFFF, BLUE = 0xFF1F4E9E, NAVY = 0xFF1B3A8C;
    static final int RED = 0xFFC8102E, GREEN = 0xFF2E8B3A, LEAF = 0xFF3FAE49;
    static final int GOLD = 0xFFF6B40E, GOLD_EDGE = 0xFF85340A, YELLOW = 0xFFF4C20D, DARK = 0xFF3A2A18;
    static final int SNOW_GRAY = 0xFF6B7B8C, SKIN = 0xFFE0B07A;

    /** Duracion (en segundos) de cada cancion. AJUSTAR a la duracion real de tu archivo .ogg. */
    static final double HIMNO_SECONDS = 90, MARCHA_SECONDS = 150;

    record Flag(String id, String en, String es, String pattern, String keys, Consumer<BufferedImage> painter) {}

    static List<Flag> flags() {
        List<Flag> l = new ArrayList<>();
        l.add(new Flag("flag_argentina", "Argentine Flag", "Bandera Argentina",
                "CCC/WWW/ S ", "C=minecraft:light_blue_wool;W=minecraft:white_wool;S=" + MOD + ":sol_de_mayo",
                AssetGenerator::argentina));
        l.add(new Flag("flag_argentina_sin_sol", "Argentine Flag (No Sun)", "Bandera Argentina sin Sol",
                "CCC/WWW/ T ", "C=minecraft:light_blue_wool;W=minecraft:white_wool;T=minecraft:stick",
                AssetGenerator::argentinaSinSol));
        l.add(new Flag("flag_confederacion", "Flag of the Argentine Confederation",
                "Bandera de la Confederaci\u00f3n Argentina",
                "BBB/WWW/ T ", "B=minecraft:blue_wool;W=minecraft:white_wool;T=minecraft:stick",
                AssetGenerator::confederacion));
        l.add(new Flag("flag_confederacion_rosas", "Flag of the Confederation (Rosas era)",
                "Bandera de la Confederaci\u00f3n (\u00e9poca de Rosas)",
                "BRB/WWW/ T ", "B=minecraft:blue_wool;R=minecraft:red_wool;W=minecraft:white_wool;T=minecraft:stick",
                AssetGenerator::confederacionRosas));
        l.add(new Flag("flag_liga_federal", "Flag of the Federal League", "Bandera de la Liga Federal",
                "BWB/WRW/ T ", "B=minecraft:blue_wool;W=minecraft:white_wool;R=minecraft:red_wool;T=minecraft:stick",
                AssetGenerator::ligaFederal));
        l.add(new Flag("flag_buenos_aires", "Flag of Buenos Aires Province", "Bandera de la Provincia de Buenos Aires",
                "BBB/GRG/ T ", "B=minecraft:blue_wool;G=minecraft:green_wool;R=minecraft:red_wool;T=minecraft:stick",
                AssetGenerator::buenosAires));
        l.add(new Flag("flag_cordoba", "Flag of C\u00f3rdoba", "Bandera de C\u00f3rdoba",
                "RWC/RWC/ N ", "R=minecraft:red_wool;W=minecraft:white_wool;C=minecraft:light_blue_wool;N=minecraft:gold_nugget",
                AssetGenerator::cordoba));
        l.add(new Flag("flag_santa_fe", "Flag of Santa Fe", "Bandera de Santa Fe",
                "RWC/RWC/ A ", "R=minecraft:red_wool;W=minecraft:white_wool;C=minecraft:light_blue_wool;A=minecraft:arrow",
                AssetGenerator::santaFe));
        l.add(new Flag("flag_mendoza", "Flag of Mendoza", "Bandera de Mendoza",
                "WWW/CCC/ T ", "W=minecraft:white_wool;C=minecraft:light_blue_wool;T=minecraft:stick",
                AssetGenerator::mendoza));
        l.add(new Flag("flag_entre_rios", "Flag of Entre R\u00edos", "Bandera de Entre R\u00edos",
                "CWC/WRW/ T ", "C=minecraft:light_blue_wool;W=minecraft:white_wool;R=minecraft:red_wool;T=minecraft:stick",
                AssetGenerator::entreRios));
        l.add(new Flag("flag_patagonia", "Flag of Patagonia (fictional)", "Bandera de la Patagonia (ficticia)",
                "CCC/BWB/ T ", "C=minecraft:light_blue_wool;B=minecraft:blue_wool;W=minecraft:white_wool;T=minecraft:stick",
                AssetGenerator::patagonia));
        return l;
    }

    // =====================================================================================
    //  Pintores (cada uno dibuja la bandera en coordenadas 0..112 x 0..72)
    // =====================================================================================

    static void argentina(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        sun(im, 56, 36, 11.5, 16, true, 0);
    }

    static void argentinaSinSol(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
    }

    /** Reconstruccion aproximada: franjas azul oscuro/blanco/azul oscuro con Sol de Mayo. */
    static void confederacion(BufferedImage im) {
        horizontal3(im, NAVY, WHITE, NAVY);
        sun(im, 56, 36, 11.5, 16, true, 0);
    }

    /** Confederacion de la epoca de Rosas: sol rojo (punzo) y gorros frigios rojos en las cuatro esquinas. */
    static void confederacionRosas(BufferedImage im) {
        horizontal3(im, NAVY, WHITE, NAVY);
        sun(im, 56, 36, 14, 24, true, 0, 0xFFD01030, 0xFF6B0A18);
        cap(im, 3, 3, false);
        cap(im, FW - 3 - 14, 3, true);
        cap(im, 3, FH - 3 - 13, false);
        cap(im, FW - 3 - 14, FH - 3 - 13, true);
    }

    /** Gorro frigio de 14x13 px: cuerpo rojo, punta curvada, banda inferior y contorno oscuro. */
    static void cap(BufferedImage im, int x, int y, boolean mirror) {
        int w = 14, h = 13;
        boolean[][] m = new boolean[h][w];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double bx = (i + 0.5 - 6) / 5.5, by = (j + 0.5 - 7) / 4.5;
                double tx = (i + 0.5 - 9) / 2.6, ty = (j + 0.5 - 3) / 2.4;
                double ux = (i + 0.5 - 11.6) / 1.9, uy = (j + 0.5 - 3.6) / 1.9;
                double vx = (i + 0.5 - 12.6) / 1.2, vy = (j + 0.5 - 6) / 1.8;
                boolean body = bx * bx + by * by <= 1.0 || tx * tx + ty * ty <= 1.0
                        || ux * ux + uy * uy <= 1.0 || vx * vx + vy * vy <= 1.0;
                boolean band = j >= 10 && j <= 12 && i >= 1 && i <= 11;
                m[j][i] = body || band;
            }
        }
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                if (!m[j][i]) continue;
                boolean edge = i == 0 || j == 0 || i == w - 1 || j == h - 1
                        || !m[j][i - 1] || !m[j][i + 1] || !m[j - 1][i] || !m[j + 1][i];
                int c = edge ? 0xFF6B0A18 : (j >= 10 ? 0xFFA00C22 : 0xFFD01030);
                set(im, x + (mirror ? w - 1 - i : i), y + j, c);
            }
        }
    }

    static void ligaFederal(BufferedImage im) {
        horizontal3(im, BLUE, WHITE, BLUE);
        diagonalRed(im);
    }

    static void entreRios(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        diagonalRed(im);
    }

    static void diagonalRed(BufferedImage im) {
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double yd = y + 0.5 - x * (FH / (double) FW);
                if (Math.abs(yd) < 7.5) set(im, x, y, RED);
            }
        }
    }

    static void buenosAires(BufferedImage im) {
        fill(im, 0, 0, FW, 36, BLUE);
        fill(im, 0, 36, FW, FH, GREEN);
        for (int x = 0; x < FW; x++) {
            if (Math.abs(x + 0.5 - 56) < 18) continue; // la linea roja se interrumpe en el centro
            fill(im, x, 34, x + 1, 38, RED);
        }
        // Mitad inferior: engranaje azul de 6 dientes y media flor de girasol
        for (int y = 36; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double dx = x + 0.5 - 56, dy = y + 0.5 - 36, d = Math.hypot(dx, dy);
                double ang = Math.atan2(dy, dx);
                boolean ring = d >= 13 && d <= 16;
                boolean tooth = false;
                for (int k = 0; k < 6; k++) {
                    double a = Math.toRadians(30 + 60 * k);
                    double da = Math.abs(Math.atan2(Math.sin(ang - a), Math.cos(ang - a)));
                    if (d > 15 && d <= 19.5 && da < Math.toRadians(10)) tooth = true;
                }
                if (ring || tooth) set(im, x, y, NAVY);
                double petals = 6.5 + 3.5 * Math.abs(Math.cos(2.5 * ang));
                if (d <= petals) set(im, x, y, d <= 4 ? GOLD_EDGE : YELLOW);
            }
        }
        // Mitad superior: sol naciente y media corona de laureles
        sun(im, 56, 36, 11, 16, false, -1);
        for (double a = 195; a <= 345; a += 7.5) {
            double r = 15.5 + ((int) ((a - 195) / 7.5) % 2 == 0 ? 1.5 : -1.0);
            int x = (int) Math.round(56 + r * Math.cos(Math.toRadians(a)));
            int y = (int) Math.round(36 + r * Math.sin(Math.toRadians(a)));
            fill(im, x - 1, y - 1, x + 1, y + 1, LEAF);
        }
    }

    static void cordoba(BufferedImage im) {
        vertical3(im, RED, WHITE, CELESTE);
        sun(im, 56, 36, 17, 32, false, 0); // sol jesuita: 32 rayos (16 rectos y 16 ondulados)
    }

    static void santaFe(BufferedImage im) {
        vertical3(im, RED, WHITE, CELESTE);
        ellipse(im, 56, 36, 18, 28, YELLOW);
        ellipse(im, 56, 36, 14, 24, 0xFFEAF2FA);
        // leyenda "Provincia invencible de Santa Fe" (sugerida con marcas oscuras)
        for (int a = 0; a < 360; a += 9) {
            if (a >= 75 && a <= 105) continue;
            if ((a / 9) % 3 == 2) continue;
            int x = (int) Math.round(56 + 16 * Math.cos(Math.toRadians(a)));
            int y = (int) Math.round(36 + 26 * Math.sin(Math.toRadians(a)));
            set(im, x, y, DARK);
        }
        // flechas en cruz de San Andres y lanza con la punta en alto
        line(im, 47, 30, 65, 56, 2, DARK);
        line(im, 65, 30, 47, 56, 2, DARK);
        line(im, 56, 22, 56, 58, 2, 0xFF7A5A2A);
        for (int i = 0; i < 6; i++) fill(im, 56 - (5 - i) / 2, 16 + i, 56 + (5 - i) / 2 + 1, 17 + i, 0xFF9AA3AD);
        // sol naciente en la parte superior
        sun(im, 56, 27, 9, 12, false, -1);
    }

    static void mendoza(BufferedImage im) {
        fill(im, 0, 0, FW, 36, WHITE);
        fill(im, 0, 36, FW, FH, CELESTE);
        // laureles
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double dx = (x + 0.5 - 56) / 18.0, dy = (y + 0.5 - 38) / 23.0;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d >= 1.0 && d <= 1.22 && y > 34) set(im, x, y, GREEN);
            }
        }
        ellipse(im, 56, 38, 14, 19, GOLD_EDGE);
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double dx = (x + 0.5 - 56) / 13.0, dy = (y + 0.5 - 38) / 18.0;
                if (dx * dx + dy * dy <= 1.0) set(im, x, y, y < 38 ? CELESTE : WHITE);
            }
        }
        line(im, 56, 22, 56, 48, 1, GOLD);                // pica
        fill(im, 52, 22, 61, 25, RED);                    // gorro frigio
        fill(im, 54, 19, 59, 22, RED);
        fill(im, 51, 40, 62, 44, SKIN);                   // manos entrelazadas
        fill(im, 55, 40, 57, 44, GOLD_EDGE);
        sun(im, 56, 19, 10, 16, false, -1);               // Sol de Mayo asomando
    }

    /** Diseno ficticio: no existe una bandera oficial unica de la Patagonia. */
    static void patagonia(BufferedImage im) {
        fill(im, 0, 0, FW, 48, CELESTE);
        fill(im, 0, 48, FW, FH, NAVY);
        // cordillera nevada
        for (int y = 12; y < 48; y++) {
            double half = (y - 12) * 0.62;
            for (int x = 0; x < FW; x++) {
                if (Math.abs(x + 0.5 - 72) <= half) set(im, x, y, y < 24 ? WHITE : SNOW_GRAY);
            }
        }
        for (int y = 26; y < 48; y++) {
            double half = (y - 26) * 0.5;
            for (int x = 0; x < FW; x++) {
                if (Math.abs(x + 0.5 - 40) <= half) set(im, x, y, y < 33 ? WHITE : SNOW_GRAY);
            }
        }
        // Cruz del Sur en dorado
        int[][] stars = {{18, 8}, {18, 34}, {8, 20}, {30, 18}, {24, 26}};
        for (int[] s : stars) {
            fill(im, s[0] - 1, s[1], s[0] + 2, s[1] + 1, GOLD);
            fill(im, s[0], s[1] - 1, s[0] + 1, s[1] + 2, GOLD);
        }
    }

    // =====================================================================================
    //  Primitivas de dibujo (sin antialiasing: pixel art)
    // =====================================================================================

    static void set(BufferedImage im, int x, int y, int c) {
        if (x >= 0 && y >= 0 && x < im.getWidth() && y < im.getHeight()) im.setRGB(x, y, c);
    }

    static void fill(BufferedImage im, int x0, int y0, int x1, int y1, int c) {
        for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) set(im, x, y, c);
    }

    static void horizontal3(BufferedImage im, int a, int b, int c) {
        fill(im, 0, 0, FW, 24, a);
        fill(im, 0, 24, FW, 48, b);
        fill(im, 0, 48, FW, FH, c);
    }

    static void vertical3(BufferedImage im, int a, int b, int c) {
        fill(im, 0, 0, 37, FH, a);
        fill(im, 37, 0, 75, FH, b);
        fill(im, 75, 0, FW, FH, c);
    }

    static void ellipse(BufferedImage im, double cx, double cy, double rx, double ry, int c) {
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) set(im, x, y, c);
            }
        }
    }

    static void line(BufferedImage im, double x0, double y0, double x1, double y1, int th, int c) {
        int n = (int) (Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)) * 2) + 1;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            int x = (int) Math.round(x0 + (x1 - x0) * t), y = (int) Math.round(y0 + (y1 - y0) * t);
            fill(im, x - th / 2, y - th / 2, x - th / 2 + th, y - th / 2 + th, c);
        }
    }

    /**
     * Sol de Mayo / sol jesuita: disco central y rayos alternados rectos y ondulados.
     * clip: 0 = completo, -1 = solo mitad superior, +1 = solo mitad inferior.
     */
    static void sun(BufferedImage im, double cx, double cy, double radius, int rays, boolean face, int clip) {
        sun(im, cx, cy, radius, rays, face, clip, GOLD, GOLD_EDGE);
    }

    static void sun(BufferedImage im, double cx, double cy, double radius, int rays, boolean face, int clip,
                    int fillColor, int edgeColor) {
        double r0 = radius * 0.46, step = 2 * Math.PI / rays;
        int w = im.getWidth(), h = im.getHeight();
        boolean[][] m = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = x + 0.5 - cx, dy = y + 0.5 - cy;
                if (clip < 0 && dy > 0) continue;
                if (clip > 0 && dy < 0) continue;
                double d = Math.hypot(dx, dy);
                if (d > radius) continue;
                if (d <= r0) { m[y][x] = true; continue; }
                double ang = Math.atan2(dy, dx);
                long k = Math.round(ang / step);
                double delta = ang - k * step;
                boolean wavy = (k & 1) != 0;
                double len = wavy ? radius * 0.9 : radius;
                if (d > len) continue;
                double lat = d * Math.sin(delta);
                if (wavy) lat += Math.sin((d - r0) * 1.4) * (0.35 + radius / 30.0);
                double base = r0 * Math.sin(step / 2) * 1.1 + 0.5;
                double wd = Math.max(0.55, base * (1 - (d - r0) / (len - r0)));
                if (Math.abs(lat) <= wd) m[y][x] = true;
            }
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!m[y][x]) continue;
                boolean edge = x == 0 || y == 0 || x == w - 1 || y == h - 1
                        || !m[y][x - 1] || !m[y][x + 1] || !m[y - 1][x] || !m[y + 1][x];
                set(im, x, y, edge ? edgeColor : fillColor);
            }
        }
        if (face && radius >= 8) {
            int ex = (int) Math.round(radius * 0.2), ey = (int) Math.round(cy - radius * 0.12);
            set(im, (int) Math.round(cx) - ex - 1, ey, edgeColor);
            set(im, (int) Math.round(cx) + ex, ey, edgeColor);
            set(im, (int) Math.round(cx) - 1, (int) Math.round(cy + radius * 0.05), edgeColor);
            int my = (int) Math.round(cy + radius * 0.22);
            for (int x = (int) Math.round(cx - radius * 0.17); x < (int) Math.round(cx + radius * 0.17); x++) set(im, x, my, edgeColor);
        }
    }

    /** Icono 32x32 de inventario: bandera reducida junto a un mastil. */
    static BufferedImage icon(BufferedImage flag) {
        BufferedImage ic = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        int dw = 26, dh = 17, ox = 5, oy = 5;
        for (int y = 0; y < dh; y++) {
            for (int x = 0; x < dw; x++) {
                int sx0 = x * FW / dw, sx1 = Math.max(sx0 + 1, (x + 1) * FW / dw);
                int sy0 = y * FH / dh, sy1 = Math.max(sy0 + 1, (y + 1) * FH / dh);
                long r = 0, g = 0, b = 0;
                int n = 0;
                for (int sy = sy0; sy < sy1; sy++) {
                    for (int sx = sx0; sx < sx1; sx++) {
                        int c = flag.getRGB(sx, sy);
                        r += (c >> 16) & 255;
                        g += (c >> 8) & 255;
                        b += c & 255;
                        n++;
                    }
                }
                ic.setRGB(ox + x, oy + y, 0xFF000000 | ((int) (r / n) << 16) | ((int) (g / n) << 8) | (int) (b / n));
            }
        }
        fill(ic, 3, 5, 5, 30, 0xFF7A5A2A);
        fill(ic, 2, 2, 6, 5, GOLD);
        return ic;
    }

    static BufferedImage poleIcon() {
        BufferedImage ic = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fill(ic, 7, 2, 9, 15, 0xFF7A5A2A);
        fill(ic, 7, 2, 8, 15, 0xFF9A7A3A);
        fill(ic, 6, 0, 10, 2, GOLD);
        return ic;
    }

    static BufferedImage disc(int top, int bottom) {
        BufferedImage im = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double d = Math.hypot(x + 0.5 - 8, y + 0.5 - 8);
                if (d <= 7.4) im.setRGB(x, y, d > 6.4 ? 0xFF2E2E2E : 0xFF111111);
                if (d <= 4.6) im.setRGB(x, y, y < 8 ? top : bottom);
                if (d <= 1.0) im.setRGB(x, y, 0xFF111111);
            }
        }
        im.setRGB(4, 3, 0xFF6A6A6A);
        im.setRGB(5, 2, 0xFF6A6A6A);
        im.setRGB(3, 4, 0xFF6A6A6A);
        return im;
    }

    // =====================================================================================
    //  Escritura de archivos
    // =====================================================================================

    static void write(Path p, String content) throws IOException {
        Files.createDirectories(p.getParent());
        Files.writeString(p, content, StandardCharsets.UTF_8);
    }

    static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '"' || ch == '\\') sb.append('\\').append(ch);
            else if (ch > 126) sb.append(String.format("\\u%04x", (int) ch));
            else sb.append(ch);
        }
        return sb.toString();
    }

    static String faces(String tex, String... names) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            sb.append(i > 0 ? ", " : "").append("\"").append(names[i]).append("\": {\"texture\": \"#").append(tex).append("\"}");
        }
        return sb.toString();
    }

    /** Mastil extensible, discos de musica, sonidos y textos de idioma asociados. */
    static void extras(Path assets, Path data, StringBuilder en, StringBuilder es) throws IOException {
        String all = faces("pole", "north", "south", "east", "west", "up", "down");

        // --- Mastil (segmento apilable) ---
        write(assets.resolve("models/block/flag_pole.json"),
                "{\n  \"parent\": \"minecraft:block/block\",\n  \"textures\": {\n"
                        + "    \"particle\": \"minecraft:block/spruce_planks\",\n    \"pole\": \"minecraft:block/spruce_planks\"\n  },\n"
                        + "  \"elements\": [\n    {\"from\": [7, 0, 7], \"to\": [9, 16, 9], \"faces\": {" + all + "}}\n  ]\n}\n");
        write(assets.resolve("blockstates/flag_pole.json"),
                "{\n  \"variants\": {\n    \"\": {\"model\": \"" + MOD + ":block/flag_pole\"}\n  }\n}\n");
        Path poleIcon = assets.resolve("textures/item/flag_pole.png");
        Files.createDirectories(poleIcon.getParent());
        ImageIO.write(poleIcon(), "png", poleIcon.toFile());
        write(assets.resolve("models/item/flag_pole.json"),
                "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\"layer0\": \"" + MOD + ":item/flag_pole\"}\n}\n");
        write(data.resolve("loot_table/blocks/flag_pole.json"),
                "{\n  \"type\": \"minecraft:block\",\n  \"pools\": [\n    {\n      \"bonus_rolls\": 0.0,\n"
                        + "      \"conditions\": [{\"condition\": \"minecraft:survives_explosion\"}],\n"
                        + "      \"entries\": [{\"type\": \"minecraft:item\", \"name\": \"" + MOD + ":flag_pole\"}],\n"
                        + "      \"rolls\": 1.0\n    }\n  ],\n  \"random_sequence\": \"" + MOD + ":blocks/flag_pole\"\n}\n");
        write(data.resolve("recipe/flag_pole.json"),
                "{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [\"T\", \"T\", \"T\"],\n"
                        + "  \"key\": {\"T\": {\"item\": \"minecraft:stick\"}},\n"
                        + "  \"result\": {\"id\": \"" + MOD + ":flag_pole\", \"count\": 3}\n}\n");
        en.append(",\n  \"block.").append(MOD).append(".flag_pole\": \"Flag Pole\"");
        es.append(",\n  \"block.").append(MOD).append(".flag_pole\": \"M\u00e1stil de bandera\"");

        // --- Discos de musica ---
        String[][] discs = {
                {"himno_nacional", "Argentine National Anthem", "Himno Nacional Argentino", String.valueOf(HIMNO_SECONDS),
                        "LWL", "WGW", "LWL", "L=minecraft:light_blue_dye;W=minecraft:white_dye;G=minecraft:gold_ingot"},
                {"marcha_san_lorenzo", "March of San Lorenzo", "Marcha de San Lorenzo", String.valueOf(MARCHA_SECONDS),
                        "RBR", "BGB", "RBR", "R=minecraft:red_dye;B=minecraft:blue_dye;G=minecraft:gold_ingot"}
        };
        int[][] colors = {{CELESTE, CELESTE}, {RED, BLUE}};
        StringBuilder sounds = new StringBuilder("{\n");
        for (int i = 0; i < discs.length; i++) {
            String[] d = discs[i];
            String id = d[0];
            Path tex = assets.resolve("textures/item/music_disc_" + id + ".png");
            Files.createDirectories(tex.getParent());
            ImageIO.write(disc(colors[i][0], colors[i][1]), "png", tex.toFile());
            write(assets.resolve("models/item/music_disc_" + id + ".json"),
                    "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\"layer0\": \"" + MOD + ":item/music_disc_" + id + "\"}\n}\n");
            write(data.resolve("jukebox_song/" + id + ".json"),
                    "{\n  \"comparator_output\": " + (i + 1) + ",\n  \"description\": {\"translate\": \"jukebox_song." + MOD + "." + id + "\"},\n"
                            + "  \"length_in_seconds\": " + d[3] + ",\n  \"sound_event\": \"" + MOD + ":music_disc." + id + "\"\n}\n");
            StringBuilder r = new StringBuilder("{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [\"")
                    .append(d[4]).append("\", \"").append(d[5]).append("\", \"").append(d[6]).append("\"],\n  \"key\": {\n");
            String[] keys = d[7].split(";");
            for (int k = 0; k < keys.length; k++) {
                String[] kv = keys[k].split("=");
                r.append("    \"").append(kv[0]).append("\": {\"item\": \"").append(kv[1]).append("\"}").append(k < keys.length - 1 ? ",\n" : "\n");
            }
            r.append("  },\n  \"result\": {\"id\": \"").append(MOD).append(":music_disc_").append(id).append("\", \"count\": 1}\n}\n");
            write(data.resolve("recipe/music_disc_" + id + ".json"), r.toString());
            sounds.append("  \"music_disc.").append(id).append("\": {\"sounds\": [{\"name\": \"").append(MOD)
                    .append(":music_disc/").append(id).append("\", \"stream\": true}]}").append(i < discs.length - 1 ? ",\n" : "\n");
            en.append(",\n  \"item.").append(MOD).append(".music_disc_").append(id).append("\": \"Music Disc\"")
                    .append(",\n  \"jukebox_song.").append(MOD).append(".").append(id).append("\": \"").append(esc(d[1])).append("\"");
            es.append(",\n  \"item.").append(MOD).append(".music_disc_").append(id).append("\": \"Disco de m\u00fasica\"")
                    .append(",\n  \"jukebox_song.").append(MOD).append(".").append(id).append("\": \"").append(esc(d[2])).append("\"");
        }
        write(assets.resolve("sounds.json"), sounds.append("}\n").toString());
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        Path out = Paths.get(args[0]);
        if (Files.exists(out)) {
            try (Stream<Path> s = Files.walk(out)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
        Path assets = out.resolve("assets/" + MOD);
        Path data = out.resolve("data/" + MOD);
        String[] dirs = {"north", "east", "south", "west"};
        int[] rot = {0, 90, 180, 270};

        List<Flag> flags = flags();
        StringBuilder en = new StringBuilder("{\n  \"itemGroup." + MOD + ".main\": \"Argentina Flags\",\n  \"item." + MOD + ".sol_de_mayo\": \"Sun of May\"");
        StringBuilder es = new StringBuilder("{\n  \"itemGroup." + MOD + ".main\": \"Argentina Flags\",\n  \"item." + MOD + ".sol_de_mayo\": \"Sol de Mayo\"");

        for (Flag f : flags) {
            // Textura
            BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            f.painter().accept(img);
            Path png = assets.resolve("textures/block/" + f.id() + ".png");
            Files.createDirectories(png.getParent());
            ImageIO.write(img, "png", png.toFile());

            // Modelos de bloque (suelo y pared) e item
            for (String kind : new String[]{"floor", "wall"}) {
                write(assets.resolve("models/block/" + f.id() + "_" + kind + ".json"),
                        "{\n  \"parent\": \"" + MOD + ":block/template_flag_" + kind + "\",\n"
                                + "  \"textures\": {\n    \"flag\": \"" + MOD + ":block/" + f.id() + "\"\n  }\n}\n");
            }
            Path iconPng = assets.resolve("textures/item/" + f.id() + ".png");
            Files.createDirectories(iconPng.getParent());
            ImageIO.write(icon(img), "png", iconPng.toFile());
            write(assets.resolve("models/item/" + f.id() + ".json"),
                    "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\"layer0\": \"" + MOD + ":item/" + f.id() + "\"}\n}\n");

            // Blockstate
            StringBuilder bs = new StringBuilder("{\n  \"variants\": {\n");
            for (int w = 0; w < 2; w++) {
                for (int i = 0; i < 4; i++) {
                    bs.append("    \"facing=").append(dirs[i]).append(",wall=").append(w == 1).append("\": {\"model\": \"")
                            .append(MOD).append(":block/").append(f.id()).append(w == 1 ? "_wall" : "_floor").append("\"");
                    if (rot[i] != 0) bs.append(", \"y\": ").append(rot[i]);
                    bs.append("}").append(w == 1 && i == 3 ? "\n" : ",\n");
                }
            }
            bs.append("  }\n}\n");
            write(assets.resolve("blockstates/" + f.id() + ".json"), bs.toString());

            // Loot table (el bloque se suelta a si mismo)
            write(data.resolve("loot_table/blocks/" + f.id() + ".json"),
                    "{\n  \"type\": \"minecraft:block\",\n  \"pools\": [\n    {\n      \"bonus_rolls\": 0.0,\n"
                            + "      \"conditions\": [{\"condition\": \"minecraft:survives_explosion\"}],\n"
                            + "      \"entries\": [{\"type\": \"minecraft:item\", \"name\": \"" + MOD + ":" + f.id() + "\"}],\n"
                            + "      \"rolls\": 1.0\n    }\n  ],\n  \"random_sequence\": \"" + MOD + ":blocks/" + f.id() + "\"\n}\n");

            // Receta
            String[] rows = f.pattern().split("/", -1);
            StringBuilder r = new StringBuilder("{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [");
            for (int i = 0; i < rows.length; i++) r.append(i > 0 ? ", " : "").append("\"").append(rows[i]).append("\"");
            r.append("],\n  \"key\": {\n");
            String[] keys = f.keys().split(";");
            for (int i = 0; i < keys.length; i++) {
                String[] kv = keys[i].split("=");
                r.append("    \"").append(kv[0]).append("\": {\"item\": \"").append(kv[1]).append("\"}").append(i < keys.length - 1 ? ",\n" : "\n");
            }
            r.append("  },\n  \"result\": {\"id\": \"").append(MOD).append(":").append(f.id()).append("\", \"count\": 1}\n}\n");
            write(data.resolve("recipe/" + f.id() + ".json"), r.toString());

            en.append(",\n  \"block.").append(MOD).append(".").append(f.id()).append("\": \"").append(esc(f.en())).append("\"");
            es.append(",\n  \"block.").append(MOD).append(".").append(f.id()).append("\": \"").append(esc(f.es())).append("\"");
        }
        extras(assets, data, en, es);
        write(assets.resolve("lang/en_us.json"), en.append("\n}\n").toString());
        write(assets.resolve("lang/es_ar.json"), es.append("\n}\n").toString());

        // Item "Sol de Mayo": textura 16x16, modelo y receta
        BufferedImage sol = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        sun(sol, 8, 8, 7.6, 16, false, 0);
        Path solPng = assets.resolve("textures/item/sol_de_mayo.png");
        Files.createDirectories(solPng.getParent());
        ImageIO.write(sol, "png", solPng.toFile());
        write(assets.resolve("models/item/sol_de_mayo.json"),
                "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\"layer0\": \"" + MOD + ":item/sol_de_mayo\"}\n}\n");
        write(data.resolve("recipe/sol_de_mayo.json"),
                "{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [\"NNN\", \"NYN\", \"NNN\"],\n"
                        + "  \"key\": {\n    \"N\": {\"item\": \"minecraft:gold_nugget\"},\n    \"Y\": {\"item\": \"minecraft:yellow_dye\"}\n  },\n"
                        + "  \"result\": {\"id\": \"" + MOD + ":sol_de_mayo\", \"count\": 1}\n}\n");

        System.out.println("AssetGenerator: " + flags.size() + " banderas generadas en " + out);
    }
}
