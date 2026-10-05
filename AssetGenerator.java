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
        nf(l, "flag_caba", "Flag of Buenos Aires City", "Bandera de la Ciudad Aut\u00f3noma de Buenos Aires",
                "WWW", "WWW", "minecraft:coal", AssetGenerator::caba);
        nf(l, "flag_canuelas", "Flag of Ca\u00f1uelas", "Bandera de Ca\u00f1uelas",
                "CCC", "WWW", "minecraft:leather", AssetGenerator::canuelas);
        nf(l, "flag_jujuy", "Flag of Jujuy", "Bandera de Jujuy",
                "WWW", "WWW", "minecraft:feather", AssetGenerator::jujuy);
        nf(l, "flag_salta", "Flag of Salta", "Bandera de Salta",
                "RRR", "RRR", "minecraft:lapis_lazuli", AssetGenerator::salta);
        nf(l, "flag_formosa", "Flag of Formosa", "Bandera de Formosa",
                "CCC", "WWW", "minecraft:honeycomb", AssetGenerator::formosa);
        nf(l, "flag_misiones", "Flag of Misiones", "Bandera de Misiones",
                "RRR", "BBB", "minecraft:slime_ball", AssetGenerator::misiones);
        nf(l, "flag_corrientes", "Flag of Corrientes", "Bandera de Corrientes",
                "CCC", "WWW", "minecraft:clay_ball", AssetGenerator::corrientes);
        nf(l, "flag_chaco", "Flag of Chaco", "Bandera del Chaco",
                "GWC", "GWC", "minecraft:cocoa_beans", AssetGenerator::chaco);
        nf(l, "flag_santiago_del_estero", "Flag of Santiago del Estero", "Bandera de Santiago del Estero",
                "CWR", "CWR", "minecraft:redstone", AssetGenerator::santiagoDelEstero);
        nf(l, "flag_tucuman", "Flag of Tucum\u00e1n", "Bandera de Tucum\u00e1n",
                "WWW", "CCC", "minecraft:sugar_cane", AssetGenerator::tucuman);
        nf(l, "flag_catamarca", "Flag of Catamarca", "Bandera de Catamarca",
                "CCC", "WWW", "minecraft:copper_ingot", AssetGenerator::catamarca);
        nf(l, "flag_la_rioja", "Flag of La Rioja", "Bandera de La Rioja",
                "CCC", "WRW", "minecraft:flint", AssetGenerator::laRioja);
        nf(l, "flag_san_juan", "Flag of San Juan", "Bandera de San Juan",
                "CCC", "WWW", "minecraft:quartz", AssetGenerator::sanJuan);
        nf(l, "flag_san_luis", "Flag of San Luis", "Bandera de San Luis",
                "WWW", "WWW", "minecraft:bone", AssetGenerator::sanLuis);
        nf(l, "flag_la_pampa", "Flag of La Pampa", "Bandera de La Pampa",
                "CCC", "WWW", "minecraft:wheat", AssetGenerator::laPampa);
        nf(l, "flag_rio_negro", "Flag of R\u00edo Negro", "Bandera de R\u00edo Negro",
                "BBB", "WWW", "minecraft:apple", AssetGenerator::rioNegro);
        nf(l, "flag_chubut", "Flag of Chubut", "Bandera de Chubut",
                "CCC", "YYY", "minecraft:string", AssetGenerator::chubut);
        nf(l, "flag_santa_cruz", "Flag of Santa Cruz", "Bandera de Santa Cruz",
                "CCC", "WWW", "minecraft:paper", AssetGenerator::santaCruz);
        nf(l, "flag_tierra_del_fuego", "Flag of Tierra del Fuego", "Bandera de Tierra del Fuego (Ushuaia)",
                "BBB", "WWW", "minecraft:glowstone_dust", AssetGenerator::tierraDelFuego);
        nf(l, "flag_malvinas", "Flag of the Malvinas Islands", "Bandera de las Islas Malvinas Argentinas",
                "CCC", "WWW", "minecraft:carrot", AssetGenerator::malvinas);
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
        vertical3(im, CELESTE, WHITE, RED);
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

    static final String KEYS_ALL = "C=minecraft:light_blue_wool;W=minecraft:white_wool;B=minecraft:blue_wool;"
            + "R=minecraft:red_wool;G=minecraft:green_wool;Y=minecraft:yellow_wool;K=minecraft:black_wool";

    /** Registra una bandera nueva: dos filas de lana, un emblema unico y un palo ("E T") en la fila de abajo. */
    static void nf(List<Flag> l, String id, String en, String es, String top, String mid, String emblem,
                   Consumer<BufferedImage> painter) {
        String pattern = top + "/" + mid + "/E T";
        StringBuilder keys = new StringBuilder();
        for (String kv : KEYS_ALL.split(";")) {
            if ((top + mid).indexOf(kv.charAt(0)) >= 0) keys.append(kv).append(";");
        }
        keys.append("E=").append(emblem).append(";T=minecraft:stick");
        l.add(new Flag(id, en, es, pattern, keys.toString(), painter));
    }

    // =====================================================================================
    //  Banderas provinciales y municipales (v1.2)
    // =====================================================================================

    static final int BORDO = 0xFF7B1E2B, SILVER = 0xFFD0D5DA, BLACK = 0xFF161616, BROWN = 0xFF7A5230, HILL = 0xFF4E7A3A;
    static final int PUNZO = 0xFFB5122E;

    /** Escudo de la Asamblea del Ano XIII, simplificado (s = escala; 1.0 = tamano del escudo de Mendoza). */
    static void shield(BufferedImage im, int cx, int cy, double s) {
        double rx = 13 * s, ry = 18 * s;
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double dx = (x + 0.5 - cx) / (rx * 1.38), dy = (y + 0.5 - (cy + 2 * s)) / (ry * 1.28);
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d >= 1.0 && d <= 1.22 && y > cy - 4 * s) set(im, x, y, GREEN);
            }
        }
        ellipse(im, cx, cy, rx + 1, ry + 1, GOLD_EDGE);
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) set(im, x, y, y < cy ? CELESTE : WHITE);
            }
        }
        line(im, cx, cy - 16 * s, cx, cy + 10 * s, 1, GOLD);
        fill(im, (int) (cx - 4 * s), (int) (cy - 16 * s), (int) (cx + 5 * s), (int) (cy - 13 * s), RED);
        fill(im, (int) (cx - 2 * s), (int) (cy - 19 * s), (int) (cx + 3 * s), (int) (cy - 16 * s), RED);
        fill(im, (int) (cx - 5 * s), (int) (cy + 2 * s), (int) (cx + 6 * s), (int) (cy + 6 * s), SKIN);
        fill(im, (int) (cx - s), (int) (cy + 2 * s), (int) (cx + s + 1), (int) (cy + 6 * s), GOLD_EDGE);
        sun(im, cx, cy - 19 * s, 10 * s, 16, false, -1);
    }

    /** Puntos distribuidos en una elipse (estrellas, hojas, espigas...). */
    static void ring(BufferedImage im, double cx, double cy, double rx, double ry, int n, int size, int c) {
        for (int i = 0; i < n; i++) {
            double a = -Math.PI / 2 + 2 * Math.PI * i / n;
            int x = (int) Math.round(cx + rx * Math.cos(a)), y = (int) Math.round(cy + ry * Math.sin(a));
            fill(im, x - size / 2, y - size / 2, x - size / 2 + size, y - size / 2 + size, c);
        }
    }

    static void star6(BufferedImage im, int cx, int cy, double r, int c) {
        for (int y = cy - (int) r - 1; y <= cy + (int) r + 1; y++) {
            for (int x = cx - (int) r - 1; x <= cx + (int) r + 1; x++) {
                double dx = x + 0.5 - cx, dy = y + 0.5 - cy, d = Math.hypot(dx, dy);
                double ang = Math.atan2(dy, dx);
                if (d <= r * (0.4 + 0.6 * Math.pow(Math.abs(Math.cos(3 * ang)), 1.5))) set(im, x, y, c);
            }
        }
    }

    static void plus(BufferedImage im, int cx, int cy, int c) {
        fill(im, cx - 1, cy, cx + 2, cy + 1, c);
        fill(im, cx, cy - 1, cx + 1, cy + 2, c);
    }

    /** Dos ramas de hojas a los lados de (cx, cy). */
    static void branches(BufferedImage im, int cx, int cy, double r, int leaves, int c) {
        for (int i = 0; i < leaves; i++) {
            double a = Math.toRadians(100 + i * (160.0 / Math.max(1, leaves - 1)));
            int lx = (int) Math.round(cx + r * Math.cos(a)), ly = (int) Math.round(cy + r * Math.sin(a));
            fill(im, lx - 1, ly - 1, lx + 2, ly + 1, c);
            fill(im, 2 * cx - lx - 1, ly - 1, 2 * cx - lx + 2, ly + 1, c);
        }
    }

    /** Aguila negra coronada del escudo de Garay (Ciudad de Buenos Aires), simplificada. */
    static void eagle(BufferedImage im, int cx, int cy) {
        ellipse(im, cx - 14, cy + 2, 12, 6, BLACK);
        ellipse(im, cx + 14, cy + 2, 12, 6, BLACK);
        ellipse(im, cx, cy + 4, 7, 11, BLACK);
        ellipse(im, cx - 3, cy - 11, 4.5, 4.5, BLACK);
        fill(im, cx - 10, cy - 12, cx - 6, cy - 10, YELLOW);
        fill(im, cx - 8, cy - 17, cx - 1, cy - 15, GOLD);
        fill(im, cx - 8, cy - 19, cx - 6, cy - 17, GOLD);
        fill(im, cx - 5, cy - 20, cx - 3, cy - 17, GOLD);
        fill(im, cx - 2, cy - 19, cx, cy - 17, GOLD);
        fill(im, cx + 12, cy + 8, cx + 15, cy + 24, RED);
        fill(im, cx + 8, cy + 13, cx + 19, cy + 16, RED);
        for (int i = 0; i < 4; i++) ellipse(im, cx - 13 + i * 8.5, cy + 25, 3.2, 2.6, BLACK);
    }

    static void caba(BufferedImage im) {
        fill(im, 0, 0, FW, FH, WHITE);
        eagle(im, 56, 34);
    }

    /** Diseno propio aproximado (no se encontro una fuente oficial): cielo, llanura y sol naciente. */
    static void canuelas(BufferedImage im) {
        fill(im, 0, 0, FW, 44, CELESTE);
        fill(im, 0, 44, FW, FH, HILL);
        sun(im, 56, 44, 13, 16, false, -1);
    }

    static void jujuy(BufferedImage im) {
        fill(im, 0, 0, FW, FH, WHITE);
        shield(im, 56, 38, 1.15);
    }

    static void salta(BufferedImage im) {
        fill(im, 0, 0, FW, FH, BORDO);
        ring(im, 56, 36, 28, 28, 23, 3, YELLOW);
        ellipse(im, 56, 36, 17, 20, GOLD_EDGE);
        ellipse(im, 56, 36, 15.5, 18.5, 0xFF1E4FA0);
        star6(im, 56, 36, 13, SILVER);
        sun(im, 56, 36, 5.5, 12, false, 0);
    }

    static void formosa(BufferedImage im) {
        fill(im, 0, 0, FW, 36, CELESTE);
        fill(im, 0, 36, FW, FH, WHITE);
        for (int x = 2; x < FW - 2; x += 4) {
            int y = 36 + (int) Math.round(2 * Math.sin(x / 5.0));
            fill(im, x - 1, y - 1, x + 2, y + 2, LEAF);
        }
        ring(im, 56, 36, 13, 13, 9, 3, YELLOW);
    }

    static void misiones(BufferedImage im) {
        fill(im, 0, 0, FW, 24, RED);
        fill(im, 0, 24, FW, 48, BLUE);
        fill(im, 0, 48, FW, FH, WHITE);
    }

    static void corrientes(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        for (int y = 24; y < 48; y++) {
            double w = 20 * (1 - Math.abs(y + 0.5 - 36) / 12.0);
            fill(im, 0, y, (int) w, y + 1, CELESTE);
        }
        shield(im, 56, 36, 0.95);
    }

    static void chaco(BufferedImage im) {
        vertical3(im, GREEN, WHITE, CELESTE);
        ring(im, 56, 36, 16, 16, 25, 2, YELLOW);
        sun(im, 56, 36, 9, 16, false, 0);
    }

    static void santiagoDelEstero(BufferedImage im) {
        fill(im, 0, 0, 22, FH, CELESTE);
        fill(im, 22, 0, 33, FH, WHITE);
        fill(im, 33, 0, 79, FH, PUNZO);
        fill(im, 79, 0, 90, FH, WHITE);
        fill(im, 90, 0, FW, FH, CELESTE);
        sun(im, 56, 36, 15, 16, false, 0);
        line(im, 56, 25, 56, 49, 2, PUNZO);
        line(im, 49, 32, 63, 32, 2, PUNZO);
    }

    static void tucuman(BufferedImage im) {
        horizontal3(im, WHITE, CELESTE, WHITE);
    }

    /** Aproximada: el diseno de 2011 lleva un sol y dos ramas de olivo; los colores de fondo no pude verificarlos. */
    static void catamarca(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        sun(im, 56, 36, 11, 32, false, 0);
        branches(im, 56, 36, 18, 8, LEAF);
    }

    /** Aproximada: franja roja federal, celeste y blanco, con ramas de laurel. */
    static void laRioja(BufferedImage im) {
        fill(im, 0, 0, FW, 26, CELESTE);
        fill(im, 0, 26, FW, 46, RED);
        fill(im, 0, 46, FW, FH, WHITE);
        branches(im, 56, 36, 17, 9, LEAF);
        sun(im, 56, 36, 6, 12, false, 0);
    }

    static void sanJuan(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        shield(im, 56, 36, 1.0);
    }

    static void sanLuis(BufferedImage im) {
        fill(im, 0, 0, FW, FH, WHITE);
        ring(im, 56, 36, 21, 27, 26, 2, LEAF);
        ellipse(im, 56, 36, 17, 23, GOLD_EDGE);
        ellipse(im, 56, 36, 15.5, 21.5, CELESTE);
        int[] hx = {46, 53, 60, 67}, hh = {10, 13, 11, 9};
        for (int k = 0; k < hx.length; k++) {
            for (int dy = 0; dy < hh[k]; dy++) {
                fill(im, hx[k] - dy, 50 - hh[k] + dy, hx[k] + dy + 1, 51 - hh[k] + dy, HILL);
            }
        }
        fill(im, 43, 50, 70, 55, HILL);
        sun(im, 56, 36, 8, 12, false, -1);
        ellipse(im, 49, 52, 3.5, 2, BROWN);
        ellipse(im, 63, 52, 3.5, 2, BROWN);
    }

    static void laPampa(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        ring(im, 56, 36, 18, 24, 22, 2, GOLD);
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                double dx = (x + 0.5 - 56) / 14.0, dy = (y + 0.5 - 36) / 20.0;
                if (dx * dx + dy * dy <= 1.0) set(im, x, y, y < 44 ? CELESTE : HILL);
            }
        }
        line(im, 56, 44, 56, 32, 2, BROWN);
        ellipse(im, 56, 30, 7, 4.5, GREEN);
    }

    static void rioNegro(BufferedImage im) {
        fill(im, 0, 0, FW, 24, BLUE);
        fill(im, 0, 24, FW, 48, WHITE);
        fill(im, 0, 48, FW, FH, GREEN);
        fill(im, 0, 0, 32, 24, 0xFF111111);
        ring(im, 16, 12, 8, 8, 13, 2, WHITE);
    }

    /** Aproximada: celeste y blanco divididos por una faja amarilla (cordillera en zigzag, mar ondulado) y un engranaje. */
    static void chubut(BufferedImage im) {
        fill(im, 0, 0, FW, 36, CELESTE);
        fill(im, 0, 36, FW, FH, WHITE);
        for (int x = 0; x < FW; x++) {
            int top = 29 + ((x / 4) % 2 == 0 ? 0 : 2);
            int bottom = 42 + (int) Math.round(2 * Math.sin(x / 5.0));
            fill(im, x, top, x + 1, bottom, YELLOW);
        }
        ring(im, 56, 36, 9, 9, 10, 3, NAVY);
        ellipse(im, 56, 36, 7, 7, NAVY);
        ellipse(im, 56, 36, 4.5, 4.5, YELLOW);
    }

    /** Aproximada: cielo celeste, base blanca y Cruz del Sur dorada. */
    static void santaCruz(BufferedImage im) {
        fill(im, 0, 0, FW, 40, CELESTE);
        fill(im, 0, 40, FW, FH, WHITE);
        int[][] stars = {{50, 8}, {50, 30}, {40, 19}, {60, 17}, {54, 37}};
        for (int[] s : stars) plus(im, s[0], s[1], GOLD);
    }

    /** Aproximada: azul, blanco y celeste con una estrella dorada. */
    static void tierraDelFuego(BufferedImage im) {
        fill(im, 0, 0, FW, 24, NAVY);
        fill(im, 0, 24, FW, 48, WHITE);
        fill(im, 0, 48, FW, FH, CELESTE);
        star6(im, 56, 36, 10, GOLD);
    }

    /** Reinterpretacion no oficial: la bandera nacional con el Sol de Mayo y las islas. */
    static void malvinas(BufferedImage im) {
        horizontal3(im, CELESTE, WHITE, CELESTE);
        sun(im, 56, 36, 11.5, 16, true, 0);
        ellipse(im, 38, 61, 13, 3, 0xFF3E5648);
        ellipse(im, 72, 62, 10, 2.5, 0xFF3E5648);
    }

    /**
     * Distancia de atenuacion de los discos. En una jukebox el volumen es 4, asi que el alcance real
     * en bloques es 4 x este valor (12 -> 48 bloques). El volumen baja linealmente con la distancia.
     * IMPORTANTE: los .ogg deben ser MONO; un audio estereo no se atenua con la distancia.
     */
    static final int ATTENUATION_DISTANCE = 12;

    static int dyeColor(String dye) {
        switch (dye) {
            case "light_blue": return CELESTE;
            case "white": return WHITE;
            case "red": return RED;
            case "blue": return BLUE;
            case "black": return 0xFF222222;
            case "pink": return 0xFFF08CB4;
            case "green": return GREEN;
            case "orange": return 0xFFF08A1E;
            case "yellow": return YELLOW;
            case "gray": return 0xFF707070;
            case "purple": return 0xFF8A3DB5;
            case "magenta": return 0xFFC84BC8;
            default: return WHITE;
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
    static BufferedImage icon(BufferedImage flag, boolean large) {
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
        if (large) {
            fill(ic, 26, 25, 31, 30, GOLD_EDGE);
            fill(ic, 27, 26, 30, 29, GOLD);
        }
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
        // {id, titulo EN, titulo ES, duracion en segundos (AJUSTAR a tu .ogg), tinte 1, tinte 2}
        String[][] discs = {
                {"himno_nacional", "Argentine National Anthem", "Himno Nacional Argentino", String.valueOf(HIMNO_SECONDS), "light_blue", "white"},
                {"marcha_san_lorenzo", "March of San Lorenzo", "Marcha de San Lorenzo", String.valueOf(MARCHA_SECONDS), "red", "blue"},
                {"marcha_malvinas", "Marcha de las Malvinas", "Marcha de las Malvinas", "180", "light_blue", "black"},
                {"avenida_camelias", "Avenida de las Camelias", "Avenida de las Camelias", "180", "pink", "green"},
                {"aurora", "Aurora", "Aurora", "180", "orange", "yellow"},
                {"pucara_malvinas", "Pucar\u00e1 de Malvinas", "Pucar\u00e1 de Malvinas", "180", "gray", "white"},
                {"sobreviviendo", "Sobreviviendo - Los del Fuego", "Sobreviviendo - Los del Fuego", "180", "red", "black"},
                {"el_grandote", "El Grandote - Mc Caco", "El Grandote - Mc Caco", "180", "purple", "black"},
                {"diablo_humahuaca", "El Diablo de Humahuaca", "El Diablo de Humahuaca", "180", "red", "yellow"},
                {"campanas_noche", "Campanas en la Noche", "Campanas en la Noche", "180", "blue", "black"},
                {"cara_tramposo", "Cara de Tramposo", "Cara de Tramposo", "180", "green", "black"},
                {"estrella_federal", "Estrella Federal", "Estrella Federal", "180", "red", "white"},
                {"reina_madre", "Reina Madre - Ra\u00fal Porchetto", "Reina Madre - Ra\u00fal Porchetto", "180", "magenta", "white"}
        };
        StringBuilder sounds = new StringBuilder("{\n");
        for (int i = 0; i < discs.length; i++) {
            String[] d = discs[i];
            String id = d[0];
            Path tex = assets.resolve("textures/item/music_disc_" + id + ".png");
            Files.createDirectories(tex.getParent());
            ImageIO.write(disc(dyeColor(d[4]), dyeColor(d[5])), "png", tex.toFile());
            write(assets.resolve("models/item/music_disc_" + id + ".json"),
                    "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\"layer0\": \"" + MOD + ":item/music_disc_" + id + "\"}\n}\n");
            write(data.resolve("jukebox_song/" + id + ".json"),
                    "{\n  \"comparator_output\": " + (i + 1) + ",\n  \"description\": {\"translate\": \"jukebox_song." + MOD + "." + id + "\"},\n"
                            + "  \"length_in_seconds\": " + d[3] + ",\n  \"sound_event\": \"" + MOD + ":music_disc." + id + "\"\n}\n");
            String dx = "minecraft:" + d[4] + "_dye", dy = "minecraft:" + d[5] + "_dye";
            write(data.resolve("recipe/music_disc_" + id + ".json"),
                    "{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [\"XYX\", \"YGY\", \"XYX\"],\n"
                            + "  \"key\": {\n    \"X\": {\"item\": \"" + dx + "\"},\n    \"Y\": {\"item\": \"" + dy + "\"},\n"
                            + "    \"G\": {\"item\": \"minecraft:gold_ingot\"}\n  },\n"
                            + "  \"result\": {\"id\": \"" + MOD + ":music_disc_" + id + "\", \"count\": 1}\n}\n");
            sounds.append("  \"music_disc.").append(id).append("\": {\"sounds\": [{\"name\": \"").append(MOD)
                    .append(":music_disc/").append(id).append("\", \"stream\": true, \"attenuation_distance\": ")
                    .append(ATTENUATION_DISTANCE).append("}]}").append(i < discs.length - 1 ? ",\n" : "\n");
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
            // Textura (compartida por la version normal y la grande)
            BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            f.painter().accept(img);
            Path png = assets.resolve("textures/block/" + f.id() + ".png");
            Files.createDirectories(png.getParent());
            ImageIO.write(img, "png", png.toFile());

            for (boolean large : new boolean[]{false, true}) {
                String vid = f.id() + (large ? "_grande" : "");
                String suffix = large ? "_large" : "";

                // Modelos de bloque (suelo y pared): solo mastil/soporte; la tela la dibuja el renderer
                for (String kind : new String[]{"floor", "wall"}) {
                    write(assets.resolve("models/block/" + vid + "_" + kind + ".json"),
                            "{\n  \"parent\": \"" + MOD + ":block/template_flag_" + kind + suffix + "\"\n}\n");
                }

                // Icono de inventario
                Path iconPng = assets.resolve("textures/item/" + vid + ".png");
                Files.createDirectories(iconPng.getParent());
                ImageIO.write(icon(img, large), "png", iconPng.toFile());
                write(assets.resolve("models/item/" + vid + ".json"),
                        "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\"layer0\": \"" + MOD + ":item/" + vid + "\"}\n}\n");

                // Blockstate
                StringBuilder bs = new StringBuilder("{\n  \"variants\": {\n");
                for (int w = 0; w < 2; w++) {
                    for (int i = 0; i < 4; i++) {
                        bs.append("    \"facing=").append(dirs[i]).append(",wall=").append(w == 1).append("\": {\"model\": \"")
                                .append(MOD).append(":block/").append(vid).append(w == 1 ? "_wall" : "_floor").append("\"");
                        if (rot[i] != 0) bs.append(", \"y\": ").append(rot[i]);
                        bs.append("}").append(w == 1 && i == 3 ? "\n" : ",\n");
                    }
                }
                bs.append("  }\n}\n");
                write(assets.resolve("blockstates/" + vid + ".json"), bs.toString());

                // Loot table (el bloque se suelta a si mismo)
                write(data.resolve("loot_table/blocks/" + vid + ".json"),
                        "{\n  \"type\": \"minecraft:block\",\n  \"pools\": [\n    {\n      \"bonus_rolls\": 0.0,\n"
                                + "      \"conditions\": [{\"condition\": \"minecraft:survives_explosion\"}],\n"
                                + "      \"entries\": [{\"type\": \"minecraft:item\", \"name\": \"" + MOD + ":" + vid + "\"}],\n"
                                + "      \"rolls\": 1.0\n    }\n  ],\n  \"random_sequence\": \"" + MOD + ":blocks/" + vid + "\"\n}\n");

                // Receta: la grande se fabrica con 4 banderas normales del mismo tipo
                if (large) {
                    write(data.resolve("recipe/" + vid + ".json"),
                            "{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [\"FF\", \"FF\"],\n"
                                    + "  \"key\": {\"F\": {\"item\": \"" + MOD + ":" + f.id() + "\"}},\n"
                                    + "  \"result\": {\"id\": \"" + MOD + ":" + vid + "\", \"count\": 1}\n}\n");
                } else {
                    String[] rows = f.pattern().split("/", -1);
                    StringBuilder r = new StringBuilder("{\n  \"type\": \"minecraft:crafting_shaped\",\n  \"category\": \"misc\",\n  \"pattern\": [");
                    for (int i = 0; i < rows.length; i++) r.append(i > 0 ? ", " : "").append("\"").append(rows[i]).append("\"");
                    r.append("],\n  \"key\": {\n");
                    String[] keys = f.keys().split(";");
                    for (int i = 0; i < keys.length; i++) {
                        String[] kv = keys[i].split("=");
                        r.append("    \"").append(kv[0]).append("\": {\"item\": \"").append(kv[1]).append("\"}").append(i < keys.length - 1 ? ",\n" : "\n");
                    }
                    r.append("  },\n  \"result\": {\"id\": \"").append(MOD).append(":").append(vid).append("\", \"count\": 1}\n}\n");
                    write(data.resolve("recipe/" + vid + ".json"), r.toString());
                }

                String nameEn = large ? "Large " + f.en() : f.en();
                String nameEs = large ? f.es() + " grande" : f.es();
                en.append(",\n  \"block.").append(MOD).append(".").append(vid).append("\": \"").append(esc(nameEn)).append("\"");
                es.append(",\n  \"block.").append(MOD).append(".").append(vid).append("\": \"").append(esc(nameEs)).append("\"");
            }
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
