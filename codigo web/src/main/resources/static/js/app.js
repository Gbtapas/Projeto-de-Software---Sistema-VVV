// Interatividade simples (Vanilla JS) — validação/UX leve.
document.addEventListener('DOMContentLoaded', function () {

    // Checkout: débito é sempre à vista (RN19) — desabilita parcelas.
    const tipo = document.getElementById('tipo');
    const parcelas = document.getElementById('parcelas');
    if (tipo && parcelas) {
        const ajustar = function () {
            if (tipo.value === 'DEBITO') {
                parcelas.value = '1';
                parcelas.setAttribute('disabled', 'disabled');
            } else {
                parcelas.removeAttribute('disabled');
            }
        };
        tipo.addEventListener('change', ajustar);
        ajustar();
    }

    // Cadastro: mantém apenas dígitos no CPF e no CEP.
    document.querySelectorAll('input[maxlength="11"], input[maxlength="8"]').forEach(function (el) {
        el.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '');
        });
    });
});
