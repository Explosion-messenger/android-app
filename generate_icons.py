import math
from PIL import Image, ImageDraw, ImageFilter

def create_explosion_logo(out_path, size=1024, is_round=False):
    # Padding factor
    pad = 0.8
    actual_size = int(size * pad)
    
    # Create black background
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0) if is_round else (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)
    
    if is_round:
        draw.ellipse([0, 0, size, size], fill=(0, 0, 0, 255))
        
    cx, cy = size / 2, size / 2
    
    # Base star polygon
    num_points = 10
    
    # Outer thick highlight border
    def draw_star(radius_outer, radius_inner, fill_color, extra_scale=1.0):
        points = []
        for i in range(num_points * 2):
            # Alternating lengths
            if i % 2 == 0:
                # Outer points
                # Give some variation to the explosion
                r = radius_outer
                if i == 0 or i == 8:
                    r = radius_outer * 1.1 * extra_scale
                elif i == 4 or i == 12:
                    r = radius_outer * 0.9 * extra_scale
            else:
                r = radius_inner * extra_scale
                
            angle = i * (math.pi * 2 / (num_points * 2)) - math.pi/2
            x = cx + math.cos(angle) * r
            y = cy + math.sin(angle) * r
            points.append((x, y))
            
        draw.polygon(points, fill=fill_color)

    # Darker outline
    draw_star(actual_size * 0.5, actual_size * 0.2, (41, 140, 160, 255), extra_scale=1.1)
    
    # Lighter inner
    draw_star(actual_size * 0.45, actual_size * 0.15, (62, 184, 200, 255))
    
    img = img.resize((size, size), Image.Resampling.LANCZOS)
    img.save(out_path)

res = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}

import os
base_res = "/home/w88/code/Messenger/android/app/src/main/res"

for name, s in res.items():
    d = os.path.join(base_res, f"mipmap-{name}")
    os.makedirs(d, exist_ok=True)
    # Regular
    create_explosion_logo(os.path.join(d, "ic_launcher.png"), size=s)
    # Round
    create_explosion_logo(os.path.join(d, "ic_launcher_round.png"), size=s, is_round=True)

# Store a high-res app icon for the repo
create_explosion_logo(os.path.join(base_res, "..", "ic_launcher-playstore.png"), size=512)
print("Icons generated successfully.")
