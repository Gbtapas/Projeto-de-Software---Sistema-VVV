import os
import random

comments = [
    "// verificando se ta tudo certo",
    "// bora testar esse cenario",
    "// garantindo que nao vai dar erro aqui",
    "// conferindo os valores retornados",
    "// mais uma checagem de rotina",
    "// garantindo a logica de negocio",
    "// teste super importante",
    "// se passar isso o resto vai de boa",
    "// checando o comportamento esperado",
    "// so pra ter certeza que ta pegando o valor certo"
]

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    new_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        new_lines.append(line)
        if line.strip().startswith('@Test'):
            # achou um teste, procura a assinatura do metodo
            i += 1
            while i < len(lines) and not (' void ' in lines[i] or '()' in lines[i]):
                new_lines.append(lines[i])
                i += 1
            
            if i < len(lines):
                # Achamos a linha da declaracao do metodo. 
                # Pode terminar com '{' ou o '{' pode estar na proxima linha.
                new_lines.append(lines[i])
                
                # Se tiver '{', adicionamos o comentario embaixo
                if '{' in lines[i]:
                    indent = lines[i][:lines[i].index(lines[i].lstrip())]
                    comment = random.choice(comments)
                    new_lines.append(f"{indent}    {comment}\n")
                else:
                    # caso o { esteja na proxima linha
                    i += 1
                    if i < len(lines):
                        new_lines.append(lines[i])
                        if '{' in lines[i]:
                            indent = lines[i][:lines[i].index(lines[i].lstrip())]
                            comment = random.choice(comments)
                            new_lines.append(f"{indent}    {comment}\n")
        i += 1
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

test_dir = r"C:\Users\CONTAPROVISÓRIA\Desktop\VVV_2.0\Codigo web\src\test\java\com\vvv\reservas"

for root, dirs, files in os.walk(test_dir):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))

print("Pronto")
