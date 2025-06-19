# 💊 App Medicamentos - Gestión de Medicación Android

Esta aplicación permite registrar medicamentos, programar horarios de toma y recibir **notificaciones/alarmas** cuando es el momento de tomar la medicación. Pensada para ayudar a usuarios a seguir sus tratamientos correctamente.

---

## 🚀 Características

- 📝 Registro de medicamentos con dosis y unidad personalizada
- ⏰ Programación flexible:
  - Diariamente
  - Varias veces al día
  - Cada X horas / días
  - Semanalmente
- 🔔 Notificaciones y alarmas locales con sonido
- 📅 Historial de tomas y control de cumplimiento
- 📱 Interfaz moderna con Jetpack Compose

---

## 📸 Capturas

---

## 🧱 Arquitectura

El proyecto sigue una arquitectura moderna basada en:

- **Jetpack Compose** para UI declarativa
- **Room** para persistencia local
- **Hilt** para inyección de dependencias
- **AlarmManager** para alarmas precisas
- **BroadcastReceiver** + **NotificationManager** para recordatorios
- **MVVM** (Model-View-ViewModel)

---

## 🛠️ Tecnologías usadas

- Kotlin
- Jetpack Compose
- AndroidX Room
- Hilt DI
- AlarmManager
- ViewModel + StateFlow
- LiveData / Flow
- Material 3

---

## 🧪 Cómo probarlo
